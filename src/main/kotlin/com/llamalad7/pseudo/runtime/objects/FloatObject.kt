package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.builtinmethods.FloatObjectClassMembers
import com.llamalad7.pseudo.utils.defaultFloatPrecision
import java.math.BigDecimal
import java.math.RoundingMode

class FloatObject(val value: BigDecimal) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = FloatObjectClassMembers.map

        @JvmStatic
        fun create(value: BigDecimal) = FloatObject(value.setScale(defaultFloatPrecision, RoundingMode.HALF_UP))
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}