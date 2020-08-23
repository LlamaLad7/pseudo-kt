package com.llamalad7.pseudo.utils

import com.llamalad7.pseudo.runtime.abstraction.BaseObject

object ErrorUtils {
    @JvmStatic
    fun unsupportedOperation(operator: String, instance: BaseObject, other: BaseObject? = null): Nothing {
        if (other == null) error("Unsupported operation '$operator' on type '${instance::class.simpleName}'")
        else error("Unsupported operation '$operator' between types '${instance::class.simpleName}' and '${other::class.simpleName}'")
    }
    @JvmStatic
    fun noAccessibleAttribute(name: String, baseObject: BaseObject, accessor: Class<*>?): Nothing = error("Object of type '${baseObject::class.simpleName}' has no attribute '$name' or it is not accessible from type '${accessor?.simpleName}'")
    @JvmStatic
    fun noPublicAttribute(name: String, baseObject: BaseObject): Nothing = error("Object of type '${baseObject::class.simpleName}' has no attribute '$name' or it is private")
    @JvmStatic
    fun noWriteableAttribute(name: String, baseObject: BaseObject, accessor: Class<*>?): Nothing = error("Object of type '${baseObject::class.simpleName}' has no writeable attribute '$name' or it is not accessible from type '${accessor?.simpleName}'")
    @JvmStatic
    fun noAccessibleObject(name: String, accessor: Class<*>?): Nothing = error("Object '$name' doesn't exist in the current scope or it is not accessible from type '${accessor?.simpleName}'")
    @JvmStatic
    fun noWriteableObject(name: String, accessor: Class<*>?): Nothing = error("A writeable object '$name' doesn't exist in the current scope or it is not accessible from type '${accessor?.simpleName}'")
}