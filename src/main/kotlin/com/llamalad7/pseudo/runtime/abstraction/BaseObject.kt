package com.llamalad7.pseudo.runtime.abstraction

import com.llamalad7.pseudo.runtime.builtinmethods.DefaultMethodImpls
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.scope.ClassScope
import com.llamalad7.pseudo.utils.ErrorUtils

abstract class BaseObject {
    var parent: BaseObject? = null

    var classScope: ClassScope? = null

    abstract fun getClassMembers(): MutableMap<String, Member>

    abstract fun getInstanceMembers(): MutableMap<String, Member>

    fun getMemberOrNull(name: String, accessor: Class<*>?): BaseObject? {
        return getInstanceMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.value ?: getClassMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.value
        ?: parent?.getMemberOrNull(name, accessor)
        ?: DefaultMethodImpls.map[name]?.value
    }

    fun getMember(name: String, accessor: Class<*>?): BaseObject {
        return getMemberOrNull(name, accessor) ?: ErrorUtils.noAccessibleAttribute(name, this, accessor)
    }

    fun hasAccessibleMember(name: String, accessor: Class<*>?): Boolean {
        return (getInstanceMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.value ?: getClassMembers()[name]?.takeIf {
            it.isPublic() || this::class.java == accessor
        }?.value
        ?: parent?.getMemberOrNull(name, accessor)
        ?: DefaultMethodImpls.map[name]) != null
    }

    fun safeSetMember(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean {
        val attempt = (getInstanceMembers()[name]?.takeIf {
            (it.isPublic() || this::class.java == accessor) && it.isWriteable()
        } ?: getClassMembers()[name]?.takeIf {
            (it.isPublic() || this::class.java == accessor) && it.isWriteable()
        })?.also {
            it.value = value
        }
        return if (attempt != null) true else parent?.safeSetMember(name, value, accessor) ?: false
    }

    fun setMember(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ) {
        if(!safeSetMember(name, value, accessor)) ErrorUtils.noWriteableAttribute(name, this, accessor)
    }

    fun attemptCall(
        args: Array<BaseObject>,
        accessor: Class<*>?
    ): BaseObject {
        if (this is CallableObject) return call(args)
        return getMemberOrNull("call", accessor)?.attemptCall(args, accessor)
            ?: ErrorUtils.noAccessibleAttribute("call", this, accessor)
    }

    fun attemptBool(accessor: Class<*>?): Boolean {
        if (this is BooleanObject) return value
        return getMemberOrNull("toBool", accessor)?.attemptCall(arrayOf(this), accessor)?.attemptBool(accessor)
            ?: ErrorUtils.noAccessibleAttribute("toBool", this, accessor)
    }

}