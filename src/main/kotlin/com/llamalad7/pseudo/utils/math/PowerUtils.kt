package com.llamalad7.pseudo.utils.math

import java.math.BigDecimal
import java.math.BigInteger

fun BigDecimal.pow(exponent: BigDecimal): BigDecimal {
    val decimalPart = exponent % BigDecimal.ONE
    return when {
        decimalPart.isZero() -> this.pow(exponent.toBigIntegerExact())
        this < BigDecimal.ZERO -> error("NaN") // Exponent can't be whole as we wouldn't have reached this point
        this.isZero() -> if (exponent > BigDecimal.ZERO) BigDecimal.ZERO else error("NaN")
        else -> BigDecimalUtil.pow(this, exponent)
    }
}

fun BigInteger.pow(exponent: BigDecimal): BigDecimal {
    val decimalPart = exponent % BigDecimal.ONE
    return when {
        decimalPart.isZero() -> this.pow(exponent.toBigIntegerExact())
        this < BigInteger.ZERO -> error("NaN") // Exponent can't be whole as we wouldn't have reached this point
        this == BigInteger.ZERO -> if (exponent > BigDecimal.ZERO) BigDecimal.ZERO else error("NaN")
        else -> BigDecimalUtil.pow(this.toBigDecimal(), exponent)
    }
}

fun BigDecimal.pow(exponent: BigInteger): BigDecimal {
    return when {
        exponent == BigInteger.ZERO -> BigDecimal.ONE
        exponent > BigInteger.ZERO -> {
            var result = BigDecimal.ONE.setScale(32)
            var i = BigInteger.ZERO
            while (i++ < exponent) {
                result *= this
            }
            result
        }
        else -> {
            var result = BigDecimal.ONE.setScale(32)
            var i = BigInteger.ZERO
            val target = -exponent
            while (i++ < target) {
                result /= this
            }
            result
        }
    }
}

fun BigInteger.pow(exponent: BigInteger): BigDecimal {
    return when {
        exponent == BigInteger.ZERO -> BigDecimal.ONE
        exponent > BigInteger.ZERO -> {
            var result = BigInteger.ONE
            var i = BigInteger.ZERO
            while (i++ < exponent) {
                result *= this
            }
            result.toBigDecimal()
        }
        else -> {
            var result = BigDecimal.ONE.setScale(32)
            var i = BigInteger.ZERO
            val target = -exponent
            val divisor = this.toBigDecimal()
            while (i++ < target) {
                result /= divisor
            }
            result
        }
    }
}

fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0