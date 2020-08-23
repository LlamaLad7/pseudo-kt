package com.llamalad7.pseudo.runtime.abstraction

interface CallableObject {
    val expectedParams: Int
    fun call(args: Array<BaseObject>): BaseObject
}