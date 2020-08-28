package com.llamalad7.pseudo.ast

import java.util.*
import kotlin.collections.HashMap

data class Error(val message: String, val position: Point)

fun PseudoFile.validate(): List<Error> {
    val errors = LinkedList<Error>()
    return errors
}