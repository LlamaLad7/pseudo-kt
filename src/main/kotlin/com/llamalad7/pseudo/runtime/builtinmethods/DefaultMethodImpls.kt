package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.runtime.objects.StringObject
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object DefaultMethodImpls {
    @Suppress("CovariantEquals")
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        return BooleanObject.create(args[0] === args[1])
    }

    @JvmStatic
    fun convertToString(args: Array<BaseObject>): BaseObject {
        return StringObject.create(args[0].toString())
    }

    val map = mutableMapOf(
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod(),
        "toString" to FunctionObject(
            this::convertToString, 1
        ).toClassMethod()
    )
}