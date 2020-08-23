package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.utils.map

object ObjectCache {
    @JvmStatic
    val trueInstance = BooleanObject(true)

    @JvmStatic
    val falseInstance = BooleanObject(false)

    @JvmStatic
    val nullInstance = NullObject()

    @JvmStatic
    val cachedIntRange = (-5).toBigInteger()..255.toBigInteger()

    @JvmStatic
    val cachedInts = cachedIntRange.map { IntObject(it) }

    @JvmStatic
    val cachedIntOffset = cachedIntRange.start.toInt()

    @JvmStatic
    val emptyString = StringObject("")
}