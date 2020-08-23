package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.builtinmethods.BooleanObjectClassMembers

class BooleanObject(val value: Boolean) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = BooleanObjectClassMembers.map

        @JvmStatic
        fun create(value: Boolean): BaseObject = if (value) ObjectCache.trueInstance else ObjectCache.falseInstance
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}