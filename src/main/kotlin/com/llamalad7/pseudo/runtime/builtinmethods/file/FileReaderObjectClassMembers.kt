package com.llamalad7.pseudo.runtime.builtinmethods.file

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.BooleanObject
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import com.llamalad7.pseudo.runtime.objects.ObjectCache
import com.llamalad7.pseudo.runtime.objects.StringObject
import com.llamalad7.pseudo.runtime.objects.file.FileReaderObject
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toClassMethod

object FileReaderObjectClassMembers {
    @JvmStatic
    fun readLine(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FileReaderObject
        return StringObject.create(instance.reader.readLine())
    }

    @JvmStatic
    fun endOfFile(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FileReaderObject
        return BooleanObject.create(!instance.reader.ready())
    }

    @JvmStatic
    fun close(args: Array<BaseObject>): BaseObject {
        val instance = args[0] as FileReaderObject
        instance.reader.close()
        return ObjectCache.nullInstance
    }

    val map = mutableMapOf(
        "readLine" to FunctionObject(
            this::readLine, 1
        ).toClassMethod(),
        "endOfFile" to FunctionObject(
            this::endOfFile, 1
        ).toClassMethod(),
        "close" to FunctionObject(
            this::close, 1
        ).toClassMethod()
    )
}