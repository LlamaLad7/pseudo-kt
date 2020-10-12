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
    /**
     * This method is invoked automatically by the JVM when it tries to execute a specific invokedynamic opcode
     * for the first time (or potentially during class load), because the JvmCompiler provided this method as the bootstrap.
     * In this method, we need to give the JVM all of the information required at the invokedynamic instruction (referred to as
     * the call site). This means we need to give it concrete information about what method to actually invoke.
     */
    @JvmStatic
    fun bootstrapGetMember(lookup: MethodHandles.Lookup, name: String, type: MethodType): CallSite {
        // Here we construct a call site that can change its target throughout the program's lifetime.
        val callSite = MutableCallSite(type)

        // The first call will be to [getMember] in this class. We use some nice MethodHandle currying
        // in order to "fill" the first and last arguments of [INIT_GET_MEMBER] with predefined values.
        // Note, these values are specific to each CALL SITE, so every invokedynamic instruction location
        // will have its own unique [MemberCache].
        // This means [INIT_GET_MEMBER]'s signature goes from
        // (MutableCallSite;BaseObject;String;Class;MemberCache;)BaseObject;
        // ->
        // (BaseObject;String;Class;)BaseObject;
        // Which, if you notice, is the same signature as the ACTUAL BaseObject#getMember call, which means
        // the call site can pass the exact same arguments, which is required, as the signature of the call site
        // can never change.
        val callSiteTarget = MethodHandles.insertArguments(INIT_GET_MEMBER.bindTo(callSite), 3, MemberCache())

        // Actually link our call site to our method handle, making sure it conforms to the correct signature.
        callSite.target = callSiteTarget.asType(type)
        return callSite
    }

    /**
     * This method is invoked when the invokedynamic opcode is actually invoked every time (as long as we don't switch
     * the call site's target method). The first and last parameter are constantly bound to the same values as
     * specified in the comments for the above method, while the middle 3 parameters are actually passed from the
     * call site each invocation.
     */
    @JvmStatic
    fun getMember(
        callSite: MutableCallSite,
        baseObject: BaseObject,
        name: String,
        accessor: Class<*>?,
        inlineCache: MemberCache
    ): BaseObject {
        // First we need to check if the cache at this call site has some values for the BaseObject
        // given to us. The documentation in the [MemberCache#isValidFor] method covers what that entails.
        if (inlineCache.isValidFor(baseObject)) {
            // If our cache is valid, we can skip any lookup, and simply return the value from the
            // cached member.
            return inlineCache.getValue()
        }

        // If we missed our cache (either it's the first time calling this method, or the cache is no longer
        // valid for some reason), then we need to actually execute the original lookup. However, this lookup
        // is slightly modified from [BaseObject#getMember] because it doesn't return the current value
        // of a member, rather it returns the member slot.
        val member = getActualMember(baseObject, name, accessor)
            ?: ErrorUtils.noAccessibleAttribute(name, baseObject, accessor)

        val newCache = if (member.second) {
            // If the member found is specific to the instance of BaseObject it is called on,
            // then we need to cache based on that, meaning changing the instance will invalidate the cache.
            InstanceMemberData(baseObject, member.first)
        } else {
            // If the located member is not specific to the instance of BaseObject, then we can be much more
            // lenient with our cache, it only needs to invalidate when the class of BaseObject passed to this
            // method changes. This means all operator functions in specific are pretty much always
            // optimized.
            ClassMemberData(baseObject::class.java, member.first)
        }

        // We need to update our inline cache to respect the new cache data.
        inlineCache.memberData = newCache

        // We've now missed our cache again, and if we've exceeded our [CACHE_MISS_THRESHOLD], then this call site is
        // megamorphic (the types passed in change too often) and is probably not gaining anything from invokedynamic.
        if (++inlineCache.cacheMisses > CACHE_MISS_THRESHOLD) {
            // We just need to fall back to the slow route, and directly link our call site to the original
            // [BaseObject#getMember] call. This is a one-way road, it will never re-optimize.
            callSite.target = REAL_GET_MEMBER
        }

        // We've cached our new data, but since this method is actually an invocation at the call site,
        // it needs to return the real value for usage, so we can pass the value we have just freshly looked up.
        return member.first.value
    }

    /**
     * Simple method to look up the member slot corresponding to an object and a name, rather than
     * the current value for that slot. The second value of the pair corresponds to whether the member is an
     * instance method (true if so).
     */
    private fun getActualMember(baseObject: BaseObject, name: String, accessor: Class<*>?): Pair<Member, Boolean>? {
        return baseObject.getInstanceMembers()[name]?.takeIf {
            it.isPublic() || baseObject::class.java == accessor
        }?.let { it to true } ?: baseObject.getClassMembers()[name]?.takeIf {
            it.isPublic() || baseObject::class.java == accessor
        }?.let { it to false } ?: baseObject.parent?.let { getActualMember(it, name, accessor) }
        ?: DefaultMethodImpls.map[name]?.let { it to false }
    }

    class MemberCache {
        /**
         * Counter to keep track of how many times a specific call site has missed its cache. We start this
         * counter at -1 because we always need to miss the cache once on the first invocation, so we won't count
         * that as a miss.
         */
        var cacheMisses = -1

        /**
         * Simple, monomorphic cache. This means we can only store information about 1 type at a time.
         * In the future it might make sense for this to become polymorphic, so we can cache data about
         * more than one instance
         */
        var memberData: MemberData? = null

        /**
         * Check if this cache is still valid for the given object. If we don't have a cache currently,
         * we're obviously invalid. Otherwise, it depends on the specific data we have cached, so
         * responsibility is delegated to [MemberData.isValidFor]
         */
        fun isValidFor(baseObject: BaseObject) = memberData?.isValidFor(baseObject) ?: false

        fun getValue() = memberData!!.member.value
    }

    abstract class MemberData(val member: Member) {
        abstract fun isValidFor(baseObject: BaseObject): Boolean
    }

    class InstanceMemberData(private val instance: BaseObject, member: Member) : MemberData(member) {
        /**
         * If we're caching based on a specific instance of BaseObject (i.e. our lookup produced an instance specific
         * member slot), then we are only still valid if the instance passed is the exact same as the one we cached for.
         */
        override fun isValidFor(baseObject: BaseObject): Boolean {
            return instance === baseObject
        }
    }

    class ClassMemberData(private val clazz: Class<out BaseObject>, member: Member) : MemberData(member) {
        /**
         * If we're caching based on just the general class of BaseObject (i.e. our lookup produced a general class
         * level member slot), then we don't care if the instance of BaseObject has changed, as long as it is of
         * the same type.
         */
        override fun isValidFor(baseObject: BaseObject): Boolean {
            return clazz == baseObject::class.java
        }
    }

    private val LOOKUP = MethodHandles.lookup()

    /**
     * The handle to our internal [DynamicGetMember.getMember] method.
     * This handle has the type (MutableCallSite;BaseObject;String;Class;MemberCache;)BaseObject;
     */
    private val INIT_GET_MEMBER = LOOKUP.findStatic(
        DynamicGetMember::class.java, "getMember", MethodType.methodType(
            BaseObject::class.java,
            MutableCallSite::class.java,
            BaseObject::class.java,
            String::class.java,
            Class::class.java,
            MemberCache::class.java
        )
    )

    /**
     * The handle to the actual [BaseObject.getMember] method.
     * This handle has the type (BaseObject;String;Class;)BaseObject;
     */
    private val REAL_GET_MEMBER = LOOKUP.findVirtual(BaseObject::class.java, "getMember", MethodType.methodType(
        BaseObject::class.java,
        String::class.java,
        Class::class.java
    ))

    /**
     * This number can be fine tuned with more thorough experimentation, values between 4-10 are plausibly optimal?
     */
    private const val CACHE_MISS_THRESHOLD = 4
}
