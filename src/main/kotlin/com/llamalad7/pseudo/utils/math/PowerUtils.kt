package com.llamalad7.pseudo.utils.math

import external.BigDecimalUtil
import java.math.BigDecimal
import java.math.BigInteger

fun BigDecimal.pow(exponent: BigDecimal): BigDecimal {
    val decimalPart = exponent % BigDecimal.ONE
    if (decimalPart.isZero()) return this.pow(exponent.toBigIntegerExact())
    return when (this.compareTo(BigDecimal.ZERO)) {
        -1 -> error("NaN") // Exponent can't be whole as we wouldn't have reached this point
        0 -> if (exponent > BigDecimal.ZERO) BigDecimal.ZERO else error("NaN")
        else -> BigDecimalUtil.pow(this, exponent)
    }
}

fun BigInteger.pow(exponent: BigDecimal): BigDecimal {
    val decimalPart = exponent % BigDecimal.ONE
    if (decimalPart.isZero()) return this.pow(exponent.toBigIntegerExact())
    return when (this.compareTo(BigInteger.ZERO)) {
        -1 -> error("NaN") // Exponent can't be whole as we wouldn't have reached this point
        0 -> if (exponent > BigDecimal.ZERO) BigDecimal.ZERO else error("NaN")
        else -> BigDecimalUtil.pow(this.toBigDecimal(), exponent)
    }
}

fun BigDecimal.pow(exponent: BigInteger): BigDecimal {
    return when (exponent.compareTo(BigInteger.ZERO)) {
        0 -> BigDecimal.ONE
        1 -> {
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
    return when (exponent.compareTo(BigInteger.ZERO)) {
        0 -> BigDecimal.ONE
        1 -> {
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

fun BigInteger.positivePow(exponent: BigInteger): BigInteger {
    var result = BigInteger.ONE
    var i = BigInteger.ZERO
    while (i++ < exponent) {
        result *= this
    }
    return result
}

fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0