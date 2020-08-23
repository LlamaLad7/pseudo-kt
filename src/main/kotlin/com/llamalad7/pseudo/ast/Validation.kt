package com.llamalad7.pseudo.ast

import java.util.*
import kotlin.collections.HashMap

data class Error(val message: String, val position: Point)

fun PseudoFile.validate(): List<Error> {
    val errors = LinkedList<Error>()
//    // check a variable is not duplicated
//    val varsByName = HashMap<String, VarDeclaration>()
//    this.specificProcess(VarDeclaration::class.java) {
//        if (varsByName.containsKey(it.varName)) {
//            errors.add(
//                Error(
//                    "Variable '${it.varName}' already declared at ${varsByName[it.varName]?.position?.start}",
//                    it.position!!.start
//                )
//            )
//        } else {
//            varsByName[it.varName] = it
//        }
//    }
//    // check a variable is not referred before being declared
//    this.specificProcess(VarReference::class.java) {
//        if (!varsByName.containsKey(it.varName) || it.isBefore(varsByName[it.varName]!!)) {
//            errors.add(Error("Variable '${it.varName}' doesn't exist at this point", it.position!!.start))
//        }
//    }
//
//    this.specificProcess(Assignment::class.java) {
//        if (!varsByName.containsKey(it.varName) || it.isBefore(varsByName[it.varName]!!)) {
//            errors.add(Error("Variable '${it.varName}' doesn't exist at this point", it.position!!.start))
//        }
//    }
    return errors
}