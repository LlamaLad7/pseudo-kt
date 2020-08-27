package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.builtinmethods.ListObjectClassMembers

class ListObject(val value: MutableList<BaseObject>) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = ListObjectClassMembers.map

        @JvmStatic
        fun create(value: Array<BaseObject>): BaseObject = ListObject(value.toMutableList())
    }

    private val instanceMembers = mutableMapOf(
        "length" to Member(Visibility.PUBLIC, Access.READABLE, IntObject.create(value.size.toBigInteger()))
    )

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}