package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.utils.ErrorUtils
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object ArrayObjectClassMembers {
    @JvmStatic
    fun set(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ArrayObject.set' must be an IntObject")
        instance.value[index.value.toInt()] = args[2]
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun get(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ArrayObject.get' must be an IntObject")
        return instance.value[index.value.toInt()]
    }

    @JvmStatic
    fun convertToString(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        return StringObject.create(buildString {
            append("[")
            for (index in instance.value.indices) {
                if (index != 0) append(", ")
                append((GlobalMethods.str(arrayOf(instance.value[index])) as StringObject).value)
            }
            append("]")
        })
    }

    val map = mutableMapOf(
        "[]".operatorName to FunctionObject(
            this::get, 2
        ).toClassMethod(),
        "[]=".operatorName to FunctionObject(
            this::set, 3
        ).toClassMethod(),
        "toString" to FunctionObject(
            this::convertToString, 1
        ).toClassMethod()
    )
}