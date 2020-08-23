package com.llamalad7.pseudo.utils

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

class InMemoryClassLoader(val classData: ByteArray, val name: String) : ClassLoader() {
//    val classData by lazy {
//        val writer = ClassWriter(cwFlags)
//        node.accept(writer)
//        writer.toByteArray()
//    }

    override fun findClass(className: String): Class<*>? {
        if (className == name.replace('/', '.'))
            return defineClass(className, classData, 0, classData.size)

        return null
    }

    fun load(): Class<*> = findClass(name.replace('/', '.'))!!
}

//fun executePayload(payload: ClassNode, args: Array<String>): ByteArray {
//    val loader = InMemoryClassLoader(payload, ClassWriter.COMPUTE_FRAMES)
//    loader.load()
//        .getDeclaredMethod("main", Array<String>::class.java)
//        .invoke(null, args)
//    return loader.classData
//}