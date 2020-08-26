package com.llamalad7.pseudo.runtime.builtinmethods

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FloatObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.runtime.objects.IntObject
import com.llamalad7.pseudo.utils.ErrorUtils
import com.llamalad7.pseudo.utils.math.pow
import com.llamalad7.pseudo.utils.toClassMethod
import com.llamalad7.pseudo.utils.operatorName
import java.math.RoundingMode

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
    fun minus(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value - other.value.toBigDecimal())
            is FloatObject -> FloatObject.create(
                instance.value - other.value
            )
            else -> ErrorUtils.unsupportedOperation("-", instance, other)
        }
    }

    @JvmStatic
    fun times(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value * other.value.toBigDecimal())
            is FloatObject -> FloatObject.create(
                instance.value * other.value
            )
            else -> ErrorUtils.unsupportedOperation("*", instance, other)
        }
    }

    @JvmStatic
    fun divide(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value.divide(other.value.toBigDecimal(), RoundingMode.HALF_UP))
            is FloatObject -> FloatObject.create(
                instance.value.divide(other.value, RoundingMode.HALF_UP)
            )
            else -> ErrorUtils.unsupportedOperation("/", instance, other)
        }
    }

    @JvmStatic
    fun power(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value.pow(other.value))
            is FloatObject -> FloatObject.create(
                instance.value.pow(other.value)
            )
            else -> ErrorUtils.unsupportedOperation("^", instance, other)
        }
    }

    @JvmStatic
    fun floorDiv(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(instance.value.setScale(0).divide(other.value.toBigDecimal(), RoundingMode.FLOOR))
            is FloatObject -> FloatObject.create(
                instance.value.setScale(0).divide(other.value, RoundingMode.FLOOR)
            )
            else -> ErrorUtils.unsupportedOperation("DIV", instance, other)
        }
    }

    @JvmStatic
    fun modulo(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return when (val other = args[1]) {
            is IntObject -> FloatObject.create(
                (instance.value % other.value.toBigDecimal()).abs()
            )
            is FloatObject -> FloatObject.create(
                (instance.value % other.value).abs()
            )
            else -> ErrorUtils.unsupportedOperation("MOD", instance, other)
        }
    }

    @JvmStatic
    fun greaterThan(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return BooleanObject.create(
            when (val other = args[1]) {
                is IntObject -> instance.value > other.value.toBigDecimal()
                is FloatObject -> instance.value > other.value
                else -> ErrorUtils.unsupportedOperation(">", instance, other)
            }
        )
    }

    @JvmStatic
    fun lessThan(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return BooleanObject.create(
            when (val other = args[1]) {
                is IntObject -> instance.value < other.value.toBigDecimal()
                is FloatObject -> instance.value < other.value
                else -> ErrorUtils.unsupportedOperation("<", instance, other)
            }
        )
    }

    @Suppress("CovariantEquals")
    @JvmStatic
    fun equals(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FloatObject
        return BooleanObject.create(
            when (val other = args[1]) {
                is IntObject -> instance.value.compareTo(other.value.toBigDecimal()) == 0
                is FloatObject -> instance.value.compareTo(other.value) == 0
                else -> false
            }
        )
    }

    val map = mutableMapOf(
        "+".operatorName to FunctionObject(
            this::plus, 2
        ).toClassMethod(),
        "-".operatorName to FunctionObject(
            this::minus, 2
        ).toClassMethod(),
        "*".operatorName to FunctionObject(
            this::times, 2
        ).toClassMethod(),
        "/".operatorName to FunctionObject(
            this::divide, 2
        ).toClassMethod(),
        "^".operatorName to FunctionObject(
            this::power, 2
        ).toClassMethod(),
        "DIV".operatorName to FunctionObject(
            this::floorDiv, 2
        ).toClassMethod(),
        "MOD".operatorName to FunctionObject(
            this::modulo, 2
        ).toClassMethod(),
        ">".operatorName to FunctionObject(
            this::greaterThan, 2
        ).toClassMethod(),
        "<".operatorName to FunctionObject(
            this::lessThan, 2
        ).toClassMethod(),
        "==".operatorName to FunctionObject(
            this::equals, 2
        ).toClassMethod()
    )
}