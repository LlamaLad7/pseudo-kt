package com.llamalad7.pseudo.runtime.scope

import com.llamalad7.pseudo.runtime.abstraction.BaseObject

abstract class Scope {
    abstract val parent: Scope?
    abstract fun getOrNull(name: String, accessor: Class<*>?): BaseObject?
    abstract fun get(name: String, accessor: Class<*>?): BaseObject
    abstract fun saftSet(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean
    abstract fun set(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    )
}