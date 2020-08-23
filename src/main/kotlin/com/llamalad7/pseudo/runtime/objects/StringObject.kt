package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.builtinmethods.StringObjectClassMembers

class StringObject(val value: String) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = StringObjectClassMembers.map

        @JvmStatic
        fun create(value: String): BaseObject =
            if (value == "") {
                ObjectCache.emptyString
            } else {
                StringObject(value)
            }
    }

    private val instanceMembers = mutableMapOf<String, Member>(
        "length" to Member(Visibility.PUBLIC, Access.READABLE, IntObject.create(value.length.toBigInteger()))
    )

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}

