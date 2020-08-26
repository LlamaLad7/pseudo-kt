package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.runtime.objects.file.FileReaderObject
import com.llamalad7.pseudo.runtime.objects.file.FileWriterObject
import com.llamalad7.pseudo.utils.ErrorUtils
import com.llamalad7.pseudo.utils.math.isZero
import com.llamalad7.pseudo.utils.toClassMethod
import com.llamalad7.pseudo.utils.toGlobalMethod

object GlobalMethods {
    // Type-casting
    @JvmStatic
    fun int(args: Array<BaseObject>): BaseObject {
        return when (val obj = args[0]) {
            is IntObject -> obj
            is FloatObject -> IntObject.create(obj.value.toBigInteger())
            is StringObject -> IntObject.create(
                obj.value.toBigIntegerOrNull()
                    ?: error("String \"${obj.value}\" cannot be converted to '${IntObject::class.simpleName}'")
            )
            else -> error("Cannot convert type '${obj::class.simpleName}' to '${IntObject::class.simpleName}'")
        }
    }

    @JvmStatic
    fun str(args: Array<BaseObject>): BaseObject {
        return when (val obj = args[0]) {
            is StringObject -> obj
            is IntObject -> StringObject.create(obj.value.toString())
            is FloatObject -> StringObject.create(
                if (obj.value.isZero()) "0.0"
                else {
                    val str = obj.value.toString()
                    if ("E" in str) str
                    else buildString {
                        append(obj.value.toString().trimEnd('0'))
                        if (this.last() == '.') append('0')
                    }
                }
            )
            is BooleanObject -> StringObject.create(obj.value.toString())
            is NullObject -> StringObject.create("null")
            else -> obj.getMember("toString", null).attemptCall(args, null) as? StringObject
                    ?: error("Method 'toString' of type '${obj::class.simpleName}' must return a StringObject")
        }
    }

    @JvmStatic
    fun float(args: Array<BaseObject>): BaseObject {
        return when (val obj = args[0]) {
            is IntObject -> FloatObject.create(obj.value.toBigDecimal())
            is FloatObject -> obj
            is StringObject -> FloatObject.create(
                obj.value.toBigDecimalOrNull()
                    ?: error("String \"${obj.value}\" cannot be converted to '${FloatObject::class.simpleName}'")
            )
            else -> error("Cannot convert type '${obj::class.simpleName}' to '${IntObject::class.simpleName}'")
        }
    }

    // Normal Functions
    @JvmStatic
    fun print(args: Array<BaseObject>): BaseObject {
        println((str(args) as StringObject).value)
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun input(args: Array<BaseObject>): BaseObject {
        print((str(args) as StringObject).value)
        return StringObject.create(readLine() ?: "")
    }

    // File I/O
    @JvmStatic
    fun openRead(args: Array<BaseObject>): BaseObject {
        val path = args[0] as? StringObject ?: error("File path must be a StringObject")
        return FileReaderObject(path.value)
    }

    @JvmStatic
    fun openWrite(args: Array<BaseObject>): BaseObject {
        val path = args[0] as? StringObject ?: error("File path must be a StringObject")
        return FileWriterObject(path.value)
    }

    val map = mutableMapOf(
        "int" to FunctionObject(
            this::int, 1
        ).toClassMethod(),
        "str" to FunctionObject(
            this::str, 1
        ).toClassMethod(),
        "float" to FunctionObject(
            this::float, 1
        ).toClassMethod(),
        "print" to FunctionObject(
            this::print, 1
        ).toClassMethod(),
        "input" to FunctionObject(
            this::input, 1
        ).toClassMethod(),
        "openRead" to FunctionObject(
            this::openRead, 1
        ).toClassMethod(),
        "openWrite" to FunctionObject(
            this::openWrite, 1
        ).toClassMethod()
    )
}