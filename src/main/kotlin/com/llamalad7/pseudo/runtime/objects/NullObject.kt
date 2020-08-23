package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member

class NullObject: BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = mutableMapOf<String, Member>()

        @JvmStatic
        fun create(): BaseObject = ObjectCache.nullInstance
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}