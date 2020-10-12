package com.llamalad7.pseudo

import com.llamalad7.pseudo.ast.validate
import com.llamalad7.pseudo.compilation.JvmCompiler
import com.llamalad7.pseudo.compilation.PseudoClassWriter
import com.llamalad7.pseudo.parsing.PseudoParserFacade
import com.llamalad7.pseudo.utils.InMemoryClassLoader
import com.llamalad7.pseudo.utils.userClassPrefix
import org.objectweb.asm.ClassWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ExampleTester {

    @Test
    fun testExamples() {
        val examples = File("examples/").listFiles()?.filter { it.extension == "psc" } ?: error("Examples not found")
        for (file in examples) {
            val code = file.readText()
            if (code.startsWith("// NOTEST")) continue

            val parsingResult = PseudoParserFacade.parse(code)
            if (!parsingResult.isCorrect()) {
                println("ERRORS in ${file.name}:")
                parsingResult.errors.forEach { println(" * L${it.position.line}: ${it.message}") }
                fail("Parsing errors")
            }
            val root = parsingResult.root!!

            val errors = root.validate()
            if (errors.isNotEmpty()) {
                println("ERRORS in ${file.name}:")
                errors.forEach { println(" * L${it.position.line}: ${it.message}") }
                fail("Validation errors")
            }
            val mainClassName = "${userClassPrefix}main/Main"
            val compiler = JvmCompiler(mainClassName)
            compiler.accept(root)

            val classMap = mutableMapOf<String, ByteArray>()
            for (clazz in compiler.classes) {
                val writer = PseudoClassWriter(ClassWriter.COMPUTE_FRAMES)
                clazz.node.accept(writer)
                val bytes = writer.toByteArray()
                classMap[clazz.name] = bytes
            }
            val loader = InMemoryClassLoader(classMap)
            val mainClass = loader.load().first { it.name == mainClassName.replace('/', '.') }

            val stream = ByteArrayOutputStream()
            val oldOut = System.out
            println("Testing ${file.name}")
            System.setOut(PrintStream(stream))
            mainClass.getDeclaredMethod("main", Array<String>::class.java)
                .invoke(null, emptyArray<String>())

            assertEquals(File("examples/expectedoutputs/${file.nameWithoutExtension}.txt").readText(), stream.toString())
            System.setOut(oldOut)
            println("Passed!")
        }
    }
}