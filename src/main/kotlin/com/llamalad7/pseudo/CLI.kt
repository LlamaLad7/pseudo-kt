package com.llamalad7.pseudo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.llamalad7.pseudo.ast.validate
import com.llamalad7.pseudo.compilation.JvmCompiler
import com.llamalad7.pseudo.parsing.PseudoParserFacade
import com.llamalad7.pseudo.runtime.scope.Scope
import com.llamalad7.pseudo.utils.InMemoryClassLoader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
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

    override fun run() {
        val inputFile = File(inputFilePath)

        val outputFile = outputFilePath?.let { File(it) } ?: File(inputFile.parent, inputFile.name + ".class")

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
        val mainClassName = "com/llamalad7/pseudo/user/main/Main"
        val compiler = JvmCompiler(mainClassName)
        println("Compiled in " + measureTimeMillis {
            compiler.accept(root)
        } + " milliseconds")

        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        compiler.classes[0].node.accept(writer)
        val bytes = writer.toByteArray()

        outputFile.writeBytes(bytes)
        if (run) {
            InMemoryClassLoader(bytes, mainClassName)
                .load()
                .getDeclaredMethod("main", Array<String>::class.java)
                .invoke(null, emptyArray<String>())
        }
    }
}

class Run : CliktCommand(help = "Execute a compiled Pseudo class file") {
    private val inputFilePath by argument(help = "Pseudo class file to execute")

    override fun run() {
        val inputFile = File(inputFilePath)

        val mainClassName = "com/llamalad7/pseudo/user/main/Main"
        InMemoryClassLoader(inputFile.readBytes(), mainClassName)
            .load()
            .getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, emptyArray<String>())
    }
}

fun main(args: Array<String>) {
    Scope::get.javaMethod // Make sure kotlin reflect initialization isn't included in compilation time

    PseudoCli().subcommands(Compile(), Run()).main(args)
}