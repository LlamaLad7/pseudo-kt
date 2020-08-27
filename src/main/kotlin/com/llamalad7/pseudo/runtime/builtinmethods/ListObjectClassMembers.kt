package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod
import java.math.BigInteger

object ListObjectClassMembers {
    @JvmStatic
    fun set(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ListObject.set' must be an IntObject")
        instance.value[index.value.toInt()] = args[2]
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun get(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ListObject.get' must be an IntObject")
        return instance.value[index.value.toInt()]
    }

    @Suppress("CovariantEquals")
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        val other = args[1] as? ListObject ?: return ObjectCache.falseInstance
        if (instance.value.size != other.value.size) return ObjectCache.falseInstance
        for ((index, item) in instance.value.withIndex()) {
            if (!item.getMember("equals", null).attemptCall(arrayOf(item, other.value[index]), null).attemptBool(null)) return ObjectCache.falseInstance
        }
        return ObjectCache.trueInstance
    }

    @JvmStatic
    fun convertToString(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        return StringObject.create(buildString {
            append("[")
            for (index in instance.value.indices) {
                if (index != 0) append(", ")
                append((GlobalMethods.str(arrayOf(instance.value[index])) as StringObject).value)
            }
            append("]")
        })
    }

    @JvmStatic
    fun append(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        instance.value.add(args[1])
        val length = instance.getInstanceMembers()["length"] ?: error("'length' attribute not found")
        length.value = IntObject.create((length.value as IntObject).value + BigInteger.ONE)
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun insert(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ListObject.insert' must be an IntObject")
        instance.value.add(index.value.toInt(), args[2])
        val length = instance.getInstanceMembers()["length"] ?: error("'length' attribute not found")
        length.value = IntObject.create((length.value as IntObject).value + BigInteger.ONE)
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun pop(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        val index = args[1] as? IntObject ?: error("Index passed to 'ListObject.pop' must be an IntObject")
        val result = instance.value.removeAt(index.value.toInt())
        val length = instance.getInstanceMembers()["length"] ?: error("'length' attribute not found")
        length.value = IntObject.create((length.value as IntObject).value - BigInteger.ONE)
        return result
    }

    @JvmStatic
    fun remove(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        var found = false
        for ((index, item) in instance.value.withIndex()) {
            if (item.getMember("equals", null).attemptCall(arrayOf(item, args[1]), null).attemptBool(null)) {
                instance.value.removeAt(index)
                found = true
                break
            }
        }
        if (!found) error("Element passed to 'ListObject.remove' was not present in the list")
        val length = instance.getInstanceMembers()["length"] ?: error("'length' attribute not found")
        length.value = IntObject.create((length.value as IntObject).value - BigInteger.ONE)
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun contains(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        for (item in instance.value) {
            if (item.getMember("equals", null).attemptCall(arrayOf(item, args[1]), null).attemptBool(null)) {
                return ObjectCache.trueInstance
            }
        }
        return ObjectCache.falseInstance
    }

    @JvmStatic
    fun copy(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as ListObject
        return ListObject(instance.value.toMutableList())
    }

    val map = mutableMapOf(
        "[]".operatorName to FunctionObject(
            this::get, 2
        ).toClassMethod(),
        "[]=".operatorName to FunctionObject(
            this::set, 3
        ).toClassMethod(),
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod(),
        "toString" to FunctionObject(
            this::convertToString, 1
        ).toClassMethod(),
        "append" to FunctionObject(
            this::append, 2
        ).toClassMethod(),
        "insert" to FunctionObject(
            this::insert, 3
        ).toClassMethod(),
        "pop" to FunctionObject(
            this::pop, 2
        ).toClassMethod(),
        "remove" to FunctionObject(
            this::remove, 2
        ).toClassMethod(),
        "contains" to FunctionObject(
            this::contains, 2
        ).toClassMethod(),
        "copy" to FunctionObject(
            this::copy, 1
        ).toClassMethod()
    )
}