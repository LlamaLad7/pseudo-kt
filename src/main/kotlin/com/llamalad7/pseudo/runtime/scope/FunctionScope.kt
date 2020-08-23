package com.llamalad7.pseudo.runtime.scope

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.utils.ErrorUtils

class FunctionScope(args: Array<BaseObject>, argNames: Array<String>, override val parent: Scope? = null) : Scope() {
    private val values = mutableMapOf<String, BaseObject>()
    init {
        for ((i, name) in argNames.withIndex()) {
            values[name] = args[i]
        }
    }

    override fun getOrNull(name: String, accessor: Class<*>?): BaseObject? {
        return values[name] ?: parent?.getOrNull(name, accessor)
    }

    override fun get(name: String, accessor: Class<*>?): BaseObject {
        return getOrNull(name, accessor) ?: ErrorUtils.noAccessibleObject(name, accessor)
    }

    override fun saftSet(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean {
        if (name in values || parent?.saftSet(name, value, accessor) != true) {
            values[name] = value
        }
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