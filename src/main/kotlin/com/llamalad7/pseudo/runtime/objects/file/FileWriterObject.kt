package com.llamalad7.pseudo.runtime.objects.file

import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.builtinmethods.BooleanObjectClassMembers
import com.llamalad7.pseudo.runtime.builtinmethods.file.FileReaderObjectClassMembers
import com.llamalad7.pseudo.runtime.builtinmethods.file.FileWriterObjectClassMembers
import com.llamalad7.pseudo.runtime.objects.ObjectCache
import java.io.*

class FileWriterObject(path: String) : BaseObject() {
    val writer = BufferedWriter(FileWriter(path))
    companion object {
        @JvmStatic
        private val classMembers = FileWriterObjectClassMembers.map
    }

    private val instanceMembers = mutableMapOf<String, Member>()

    override fun getInstanceMembers() = instanceMembers

    override fun getClassMembers() =
        classMembers
}