package com.llamalad7.pseudo.runtime.objects

import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.builtinmethods.ArrayObjectClassMembers

class ArrayObject(val value: Array<BaseObject>) : BaseObject() {
    companion object {
        @JvmStatic
        private val classMembers = ArrayObjectClassMembers.map

        @JvmStatic
        fun create(value: Array<BaseObject>): BaseObject = ArrayObject(value)

        @JvmStatic
        fun makeWithDimensions(dimensions: Array<BaseObject>): BaseObject {
            val dimensions = dimensions.map { (it as? IntObject)?.value?.toInt() ?: error("Array dimensions must be of type IntObject") }
            lateinit var array: BaseObject
            for (index in dimensions.indices.reversed()) {
                array = if (index == dimensions.size - 1) ArrayObject((1..dimensions[index]).map { ObjectCache.nullInstance }.toTypedArray())
                else ArrayObject((1..dimensions[index]).map { ArrayObject((array as ArrayObject).value.copyOf()) }.toTypedArray())
            }
            return array
        }
    }

    private val instanceMembers = mutableMapOf(
        "length" to Member(Visibility.PUBLIC, Access.READABLE, IntObject.create(value.size.toBigInteger()))
    )

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}