package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object BooleanObjectClassMembers {
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as BooleanObject
        return BooleanObject.create(when(val other = args[1]) {
            is BooleanObject -> instance.value == other.value
            else -> false
        })
    }

    val map = mutableMapOf(
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod()
    )
}