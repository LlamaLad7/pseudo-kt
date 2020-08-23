package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.CallableObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class FunctionObject(value: Method?, override val expectedParams: Int) : BaseObject(),
    CallableObject {

    constructor(value: KFunction<*>, expectedParams: Int): this(value.javaMethod, expectedParams)

    val value = value ?: error("Internal error. Method not found")

    companion object {
        @JvmStatic
        private val classMembers = mutableMapOf<String, Member>()

        @JvmStatic
        fun create(value: Method?, expectedParams: Int) = FunctionObject(value, expectedParams)
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers

    override fun call(args: Array<BaseObject>): BaseObject {
        if (args.size != expectedParams) error("Expected $expectedParams parameters but got ${args.size}")
        return value.invoke(null, args) as BaseObject
    }
}