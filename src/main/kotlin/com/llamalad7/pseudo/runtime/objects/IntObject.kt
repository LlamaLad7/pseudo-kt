package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.builtinmethods.IntObjectClassMembers
import java.math.BigInteger

class IntObject(val value: BigInteger) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = IntObjectClassMembers.map

        @JvmStatic
        fun create(value: BigInteger): BaseObject =
            if (value in ObjectCache.cachedIntRange) {
                ObjectCache.cachedInts[value.toInt() - ObjectCache.cachedIntOffset]
            } else {
                IntObject(value)
            }
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}

