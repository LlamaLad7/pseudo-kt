package com.llamalad7.pseudo.runtime.scope

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.utils.ErrorUtils

class ClassScope(val obj: BaseObject, override val parent: Scope? = null) : Scope() {
    //val values = mutableMapOf<String, BaseObject>()
    override fun getOrNull(name: String, accessor: Class<*>?): BaseObject? {
        return obj.getMemberOrNull(name, accessor) ?: parent?.getOrNull(name, accessor)
    }

    override fun get(name: String, accessor: Class<*>?): BaseObject {
        return getOrNull(name, accessor) ?: ErrorUtils.noAccessibleObject(name, accessor)
    }

    override fun saftSet(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean {
        return if (obj.safeSetMember(name, value, accessor)) true else parent?.saftSet(name, value, accessor) ?: false
    }

    override fun set(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ) {
        if (!saftSet(name, value, accessor)) ErrorUtils.noWriteableObject(name, accessor)
    }
}