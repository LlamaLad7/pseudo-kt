package com.llamalad7.pseudo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.llamalad7.pseudo.ast.validate
import com.llamalad7.pseudo.compilation.JvmCompiler
import com.llamalad7.pseudo.compilation.PseudoClassWriter
import com.llamalad7.pseudo.parsing.PseudoParserFacade
import com.llamalad7.pseudo.runtime.scope.Scope
import com.llamalad7.pseudo.utils.InMemoryClassLoader
import com.llamalad7.pseudo.utils.userClassPrefix
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.jvm.javaMethod
import kotlin.system.measureTimeMillis


class PseudoCli : CliktCommand(name = "pseudo") {
    override fun run() {

    }
}

class Compile : CliktCommand(help = "Compile a Pseudo source file") {
    private val inputFilePath by argument(help = "Pseudo source file to compile")

    private val outputFilePath by argument(help = "File to write compiled class to").optional()

    private val run by option(help = "Execute the program after compiling it").flag()

    private val time by option(help = "Make the program print its execution time when it is finished").flag()

    private val debug by option(help = "Output debug information from compilation").flag()

    override fun run() {
        val inputFile = File(inputFilePath)

        val outputFile =
            outputFilePath?.let { File(it) } ?: File(inputFile.parent, inputFile.name.removeSuffix(".psc") + ".psa")

        val code = FileInputStream(inputFile)

        val parsingResult = PseudoParserFacade.parse(code)
        if (!parsingResult.isCorrect()) {
            println("ERRORS:")
            parsingResult.errors.forEach { println(" * L${it.position.line}: ${it.message}") }
            return
        }
        val root = parsingResult.root!!

        val errors = root.validate()
        if (errors.isNotEmpty()) {
            println("ERRORS:")
            errors.forEach { println(" * L${it.position.line}: ${it.message}") }
            return
        }
        val mainClassName = "${userClassPrefix}main/Main"
        val compiler = JvmCompiler(mainClassName)
        println("Compiled in " + measureTimeMillis {
            compiler.accept(root, time)
        } + " milliseconds")

        val classMap = mutableMapOf<String, ByteArray>()
        for (clazz in compiler.classes) {
            val writer = PseudoClassWriter(ClassWriter.COMPUTE_FRAMES)
            clazz.node.accept(writer)
            val bytes = writer.toByteArray()
            classMap[clazz.name] = bytes
        }
        ZipOutputStream(outputFile.outputStream()).use { zipOutputStream ->
            for ((name, byteArray) in classMap.entries) {
                val entry = ZipEntry("$name.class")
                entry.size = byteArray.size.toLong()
                zipOutputStream.putNextEntry(entry)
                zipOutputStream.write(byteArray)
                zipOutputStream.closeEntry()
            }
        }
        if (debug) {
            val debugFolder = File("DEBUG")
            debugFolder.deleteRecursively()
            debugFolder.mkdir()
            for ((name, byteArray) in classMap.entries) {
                val classFile = File(debugFolder, "${name.substringAfterLast('/')}.class")
                classFile.writeBytes(byteArray)
            }
        }
        if (run) {
            val loader = InMemoryClassLoader(classMap)
            val mainClass = loader.load().first { it.name == mainClassName.replace('/', '.') }
            mainClass.getDeclaredMethod("main", Array<String>::class.java)
                .invoke(null, emptyArray<String>())
        }
    }
}

class Run : CliktCommand(help = "Execute a compiled Pseudo archive") {
    private val inputFilePath by argument(help = "Pseudo archive to execute")

    override fun run() {
        val inputFile = File(inputFilePath)

        val classMap = mutableMapOf<String, ByteArray>()

        ZipInputStream(inputFile.inputStream()).use { zipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                classMap[entry.name.removeSuffix(".class")] = zipInputStream.readBytes()
                zipInputStream.closeEntry()
            }
        }

        val mainClassName = "${userClassPrefix.replace('/', '.')}main.Main"
        InMemoryClassLoader(classMap)
            .load()
            .first { it.name == mainClassName }
            .getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, emptyArray<String>())
    }
}

fun main(args: Array<String>) {
    Scope::get.javaMethod // Make sure kotlin reflect initialization isn't included in compilation time

    PseudoCli().subcommands(Compile(), Run()).main(args)
}
