package com.llamalad7.pseudo.runtime.scope

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.utils.ErrorUtils

object TopLevelScope: Scope() {
    private val values = mutableMapOf<String, BaseObject>()

    override val parent = GlobalScope

    override fun getOrNull(name: String, accessor: Class<*>?): BaseObject? {
        return values[name] ?: parent.getOrNull(name, accessor)
    }

    override fun get(name: String, accessor: Class<*>?): BaseObject {
        return getOrNull(name, accessor) ?: ErrorUtils.noAccessibleObject(name, accessor)
    }

    override fun saftSet(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean {
        values[name] = value
        return true
    }

    override fun set(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ) {
        if (!saftSet(name, value, accessor)) ErrorUtils.noWriteableObject(name, accessor)
    }
}