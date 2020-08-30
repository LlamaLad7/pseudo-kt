package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.utils.ErrorUtils
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object StringObjectClassMembers {
    @JvmStatic
    fun plus(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as StringObject
        return when (val other = args[1]) {
            is StringObject -> StringObject.create(
                instance.value + other.value
            )
            else -> ErrorUtils.unsupportedOperation("+", instance, other)
        }
    }

    @JvmStatic
    fun get(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as StringObject
        val index = args[1] as? IntObject ?: error("Index passed to 'StringObject.get' must be an IntObject")
        return StringObject.create(instance.value[index.value.toInt()].toString())
    }

    @Suppress("CovariantEquals")
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as StringObject
        return BooleanObject.create(when(val other = args[1]) {
            is StringObject -> instance.value == other.value
            else -> false
        })
    }

    @JvmStatic
    fun subString(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as StringObject
        val startingPosition = args[1] as? IntObject ?: error("Arguments passed to 'String.subString' must be IntObjects")
        val length = args[2] as? IntObject ?: error("Arguments passed to 'String.subString' must be IntObjects")
        return StringObject.create(instance.value.substring(startingPosition.value.toInt(), startingPosition.value.toInt() + length.value.toInt()))
    }

    val map = mutableMapOf(
        "+".operatorName to FunctionObject(
            this::plus, 2
        ).toClassMethod(),
        "[]".operatorName to FunctionObject(
            this::get, 2
        ).toClassMethod(),
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod(),
        "subString" to FunctionObject(
            this::subString, 3
        ).toClassMethod()
    )
}