package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FloatObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.runtime.objects.IntObject
import com.llamalad7.pseudo.utils.ErrorUtils
import com.llamalad7.pseudo.utils.toClassMethod
import com.llamalad7.pseudo.utils.operatorName

object FloatObjectClassMembers {
    @JvmStatic
    fun plus(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value + other.value.toBigDecimal())
            is FloatObject -> FloatObject.create(
                instance.value + other.value
            )
            else -> ErrorUtils.unsupportedOperation("+", instance, other)
        }
    }

    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return BooleanObject.create(when(val other = args[1]) {
            is IntObject -> instance.value == other.value.toBigDecimal()
            is FloatObject -> instance.value == other.value
            else -> false
        })
    }

    val map = mutableMapOf(
        "+".operatorName to FunctionObject(
            this::plus, 2
        ).toClassMethod(),
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod()
    )
}