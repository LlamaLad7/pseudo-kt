package com.llamalad7.pseudo.utils

import codes.som.anthony.koffee.ClassAssembly
import codes.som.anthony.koffee.MethodAssembly
import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.invokestatic
import codes.som.anthony.koffee.modifiers.Modifiers
import codes.som.anthony.koffee.types.TypeLike
import com.llamalad7.pseudo.ast.*
import com.llamalad7.pseudo.generated.PseudoParser
import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.objects.FunctionObject
import org.antlr.v4.runtime.Token
import org.objectweb.asm.Opcodes
import java.math.BigInteger
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

val String.unescaped: String
    get() {
        val sb = StringBuilder(this.length)
        var i = 0
        loop@ while (i < this.length) {
            var ch = this[i]
            if (ch == '\\') {
                val nextChar = if (i == this.length - 1) '\\' else this[i + 1]
                // Octal escape?
                if (nextChar in '0'..'7') {
                    var code = "" + nextChar
                    i++
                    if (i < this.length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                        code += this[i + 1]
                        i++
                        if (i < this.length - 1 && this[i + 1] >= '0' && this[i + 1] <= '7') {
                            code += this[i + 1]
                            i++
                        }
                    }
                    sb.append(code.toInt(8).toChar())
                    i++
                    continue
                }
                when (nextChar) {
                    '\\' -> ch = '\\'
                    'b' -> ch = '\b'
                    //'f' -> ch = '\f'
                    'n' -> ch = '\n'
                    'r' -> ch = '\r'
                    't' -> ch = '\t'
                    '\"' -> ch = '\"'
                    '\'' -> ch = '\''
                    'u' -> {
                        if (i >= this.length - 5) {
                            break@loop
                        }
                        val code =
                            ("" + this[i + 2] + this[i + 3]
                                    + this[i + 4] + this[i + 5]).toInt(16)
                        sb.append(Character.toChars(code))
                        i += 5
                        i++
                        continue@loop
                    }
                }
                i++
            }
            sb.append(ch)
            i++
        }
        return sb.toString()
    }

fun FunctionObject.toClassMethod() = Member(Visibility.PUBLIC, Access.READABLE, this)

fun FunctionObject.toGlobalMethod() = Member(Visibility.PUBLIC, Access.WRITEABLE, this)

fun <R> ClosedRange<BigInteger>.map(transformation: (BigInteger) -> R): List<R> {
    val result = mutableListOf<R>()
    var current = this.start
    val limit = this.endInclusive + BigInteger.ONE
    while (current != limit) {
        result.add(transformation(current))
        current += BigInteger.ONE
    }
    return result
}

fun newClassAssembly(
    access: Modifiers,
    name: String,
    version: Int = Opcodes.V1_8,
    superName: String = "java/lang/Object",
    interfaces: List<TypeLike> = listOf()
): ClassAssembly {
    lateinit var classAssembly: ClassAssembly
    assembleClass(access, name, version, superName, interfaces) {
        classAssembly = this
    }
    return classAssembly
}

val String.operatorName
    get() = operatorFunctionMap[this] ?: error("Couldn't find function mapping for operator '$this'")

fun MethodAssembly.invokejvmstatic(kFunction: KFunction<*>) {
    val method = kFunction.javaMethod ?: error("No java method corresponds to KFunction")
    invokestatic(
        method.declaringClass.name.split("$")[0].replace(".", "/"),
        method.name,
        method.returnType,
        *method.parameterTypes
    )
}

fun MethodAssembly.invokestaticgetter(kProperty: KProperty<*>) {
    val getter = kProperty.javaGetter ?: error("No java method corresponds to KProperty")
    invokestatic(
        getter.declaringClass.name.split("$")[0].replace(".", "/"),
        getter.name,
        getter.returnType,
        *getter.parameterTypes
    )
}

fun PseudoParser.SingleExpressionContext.toMemberCallExpression(
    operator: Token,
    left: PseudoParser.SingleExpressionContext,
    right: PseudoParser.SingleExpressionContext,
    considerPosition: Boolean
) = FunctionCallExpression(
    MemberExpression(
        left.toAst(considerPosition),
        operator.text.operatorName,
        if (considerPosition) Position(left.start.startPoint(), operator.endPoint()) else null
    ),
    listOf(right.toAst(considerPosition)),
    toPosition(considerPosition)
)

fun PseudoParser.SingleExpressionContext.toMemberCallExpression(
    operator: String,
    left: PseudoParser.SingleExpressionContext,
    right: PseudoParser.SingleExpressionContext,
    considerPosition: Boolean
) = FunctionCallExpression(
    MemberExpression(
        left.toAst(considerPosition),
        operator.operatorName,
        left.toPosition(considerPosition)
    ),
    listOf(right.toAst(considerPosition)),
    toPosition(considerPosition)
)