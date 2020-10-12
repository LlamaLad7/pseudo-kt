package com.llamalad7.pseudo.compilation

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.utils.userClassPrefix
import org.objectweb.asm.ClassWriter
import kotlin.reflect.jvm.jvmName

private val baseObjectName = BaseObject::class.jvmName.replace('.', '/')

class PseudoClassWriter(flags: Int) : ClassWriter(flags) {
    private fun String.extendsBaseObject() = this == baseObjectName || "${substringBeforeLast('/')}/" == userClassPrefix

    override fun getCommonSuperClass(type1: String, type2: String): String {
        return when {
            type1 == type2 -> type1
            type1.extendsBaseObject() && type2.extendsBaseObject() -> baseObjectName
            else -> super.getCommonSuperClass(type1, type2)
        }
    }
}