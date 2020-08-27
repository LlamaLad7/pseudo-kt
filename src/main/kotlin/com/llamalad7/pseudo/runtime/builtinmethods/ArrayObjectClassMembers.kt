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

    @Suppress("CovariantEquals")
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        val other = args[1] as? ArrayObject ?: return ObjectCache.falseInstance
        if (instance.value.size != other.value.size) return ObjectCache.falseInstance
        for ((index, item) in instance.value.withIndex()) {
            if (!item.getMember("equals", null).attemptCall(arrayOf(item, other.value[index]), null).attemptBool(null)) return ObjectCache.falseInstance
        }
        return ObjectCache.trueInstance
    }

    @JvmStatic
    fun convertToString(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        return StringObject.create(buildString {
            append("{")
            for (index in instance.value.indices) {
                if (index != 0) append(", ")
                append((GlobalMethods.str(arrayOf(instance.value[index])) as StringObject).value)
            }
            append("}")
        })
    }

    @JvmStatic
    fun contains(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        for (item in instance.value) {
            if (item.getMember("equals", null).attemptCall(arrayOf(item, args[1]), null).attemptBool(null)) {
                return ObjectCache.trueInstance
            }
        }
        return ObjectCache.falseInstance
    }

    @JvmStatic
    fun copy(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ArrayObject
        return ArrayObject.create(instance.value.clone())
    }

    val map = mutableMapOf(
        "[]".operatorName to FunctionObject(
            this::get, 2
        ).toClassMethod(),
        "[]=".operatorName to FunctionObject(
            this::set, 3
        ).toClassMethod(),
        "equals" to FunctionObject(
            this::equals, 2
        ).toClassMethod(),
        "toString" to FunctionObject(
            this::convertToString, 1
        ).toClassMethod(),
        "contains" to FunctionObject(
            this::convertToString, 2
        ).toClassMethod(),
        "copy" to FunctionObject(
            this::copy, 1
        ).toClassMethod()
    )
}