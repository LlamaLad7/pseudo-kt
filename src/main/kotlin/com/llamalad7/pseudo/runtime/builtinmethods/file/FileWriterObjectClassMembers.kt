package com.llamalad7.pseudo.runtime.builtinmethods.file

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.runtime.objects.ObjectCache
import com.llamalad7.pseudo.runtime.objects.StringObject
import com.llamalad7.pseudo.runtime.objects.file.FileReaderObject
import com.llamalad7.pseudo.runtime.objects.file.FileWriterObject
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object FileWriterObjectClassMembers {
    @JvmStatic
    fun writeLine(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FileWriterObject
        org.objectweb.asm.Type.LONG
        val other = args[1] as? StringObject ?: error("Argument passed to 'FileWriterObject.writeLine' must be a StringObject")
        instance.writer.write(other.value)
        instance.writer.newLine()
        return ObjectCache.nullInstance
    }

    @JvmStatic
    fun close(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FileWriterObject
        instance.writer.close()
        return ObjectCache.nullInstance
    }

    val map = mutableMapOf(
        "writeLine" to FunctionObject(
            this::writeLine, 2
        ).toClassMethod(),
        "close" to FunctionObject(
            this::close, 1
        ).toClassMethod()
    )
}