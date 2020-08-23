package com.llamalad7.pseudo.runtime.scope

import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.builtinmethods.GlobalMethods
import com.llamalad7.pseudo.utils.ErrorUtils

object GlobalScope: Scope() {
    val values = GlobalMethods.map
    override val parent: Scope? = null
    override fun getOrNull(name: String, accessor: Class<*>?): BaseObject? {
        return values[name]?.takeIf { it.isPublic() }?.value
    }

    override fun get(name: String, accessor: Class<*>?): BaseObject {
        return getOrNull(name, accessor) ?: ErrorUtils.noAccessibleObject(name, accessor)
    }

    override fun saftSet(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ): Boolean {
        return values[name]?.takeIf { it.isPublic() && it.isWriteable() }?.also { it.value = value } != null
    }

    override fun set(
        name: String,
        value: BaseObject,
        accessor: Class<*>?
    ) {
        values[name] = Member(Visibility.PUBLIC, Access.WRITEABLE, value)
    }
}