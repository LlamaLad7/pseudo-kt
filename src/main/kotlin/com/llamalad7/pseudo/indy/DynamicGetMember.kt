package com.llamalad7.pseudo.indy

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.builtinmethods.DefaultMethodImpls
import com.llamalad7.pseudo.utils.ErrorUtils
import java.lang.invoke.CallSite
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.MutableCallSite

object DynamicGetMember {
    @JvmStatic
    fun bootstrapGetMember(lookup: MethodHandles.Lookup, name: String, type: MethodType): CallSite {
        val callSite = MutableCallSite(type)

        // The first call will be to [getMember] in this class.
        val callSiteTarget = MethodHandles.insertArguments(INIT_GET_MEMBER.bindTo(callSite), 3, MemberCache())

        callSite.target = callSiteTarget.asType(type)
        return callSite
    }

    @JvmStatic
    fun getMember(
        callSite: MutableCallSite,
        baseObject: BaseObject,
        name: String,
        accessor: Class<*>?,
        inlineCache: MemberCache
    ): BaseObject {
        if (inlineCache.isValidFor(baseObject)) {
            return inlineCache.getValue()
        }

        val member = getActualMember(baseObject, name, accessor)
            ?: ErrorUtils.noAccessibleAttribute(name, baseObject, accessor)

        val newCache = if (member.second) {
            InstanceMemberData(baseObject, member.first)
        } else {
            ClassMemberData(baseObject::class.java, member.first)
        }

        inlineCache.memberData = newCache
        if (++inlineCache.cacheMisses > CACHE_MISS_THRESHOLD) {
            // We've missed our cache too much, this is a megamorphic call site,
            // so we just need to fall back to the slow route
            callSite.target = REAL_GET_MEMBER
        }

        return member.first.value
    }

    /**
     * The second value of the pair corresponds to whether the member is an instance method (true if so).
     */
    fun getActualMember(baseObject: BaseObject, name: String, accessor: Class<*>?): Pair<Member, Boolean>? {
        return baseObject.getInstanceMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.let { it to true } ?: baseObject.getClassMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.let { it to false } ?: baseObject.parent?.let { getActualMember(it, name, accessor) }
        ?: DefaultMethodImpls.map[name]?.let { it to false }
    }

    class MemberCache {
        var cacheMisses = -1
        // Simple, monomorphic cache
        var memberData: MemberData? = null

        fun isValidFor(baseObject: BaseObject) = memberData?.isValidFor(baseObject) ?: false

        fun getValue() = memberData!!.member.value
    }

    abstract class MemberData(val member: Member) {
        abstract fun isValidFor(baseObject: BaseObject): Boolean
    }

    class InstanceMemberData(private val instance: BaseObject, member: Member) : MemberData(member) {
        override fun isValidFor(baseObject: BaseObject): Boolean {
            return instance === baseObject
        }
    }

    class ClassMemberData(private val clazz: Class<out BaseObject>, member: Member) : MemberData(member) {
        override fun isValidFor(baseObject: BaseObject): Boolean {
            return clazz == baseObject::class.java
        }
    }

    val LOOKUP = MethodHandles.lookup()
    val INIT_GET_MEMBER = LOOKUP.findStatic(
        DynamicGetMember::class.java, "getMember", MethodType.methodType(
            BaseObject::class.java,
            MutableCallSite::class.java,
            BaseObject::class.java,
            String::class.java,
            Class::class.java,
            MemberCache::class.java
        )
    )
    val REAL_GET_MEMBER = LOOKUP.findVirtual(BaseObject::class.java, "getMember", MethodType.methodType(
        BaseObject::class.java,
        String::class.java,
        Class::class.java
    ))

    private const val CACHE_MISS_THRESHOLD = 4
}
