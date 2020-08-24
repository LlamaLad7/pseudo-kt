package com.llamalad7.pseudo.utils

class InMemoryClassLoader(val classes: Map<String, ByteArray>) : ClassLoader() {
    override fun findClass(className: String): Class<*>? {
        if (className in classes.keys.map { it.replace('/', '.') }){
            val name = className.replace('.', '/')
            return defineClass(className, classes[name], 0, classes[name]?.size ?: 0)
        }

        return null
    }

    fun load(): List<Class<*>> = classes.keys.map { findClass(it.replace('/', '.'))!! }
}