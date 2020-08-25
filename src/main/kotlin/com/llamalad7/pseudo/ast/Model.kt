package com.llamalad7.pseudo.ast

import com.llamalad7.pseudo.runtime.abstraction.Visibility
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

interface Node {
    val position: Position?
}

fun Node.process(operation: (Node) -> Unit) {
    operation(this)
    this.javaClass.kotlin.memberProperties.forEach { p ->
        val v = p.get(this)
        when (v) {
            is Node -> v.process(operation)
            is Collection<*> -> v.forEach { if (it is Node) it.process(operation) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.specificProcess(klass: Class<T>, operation: (T) -> Unit) {
    process {
        if (klass.isInstance(it)) {
            operation(it as T)
        }
    }
}

fun Node.transform(operation: (Node) -> Node): Node {
    operation(this)
    val changes = HashMap<String, Any>()
    this.javaClass.kotlin.memberProperties.forEach { p ->
        val v = p.get(this)
        when (v) {
            is Node -> {
                val newValue = v.transform(operation)
                if (newValue != v) changes[p.name] = newValue
            }
            is Collection<*> -> {
                val newValue = v.map { if (it is Node) it.transform(operation) else it }
                if (newValue != v) changes[p.name] = newValue
            }
        }
    }
    var instanceToTransform = this
    if (!changes.isEmpty()) {
        val constructor = this.javaClass.kotlin.primaryConstructor!!
        val params = HashMap<KParameter, Any?>()
        constructor.parameters.forEach { param ->
            if (changes.containsKey(param.name)) {
                params[param] = changes[param.name]
            } else {
                params[param] = this.javaClass.kotlin.memberProperties.find { param.name == it.name }!!.get(this)
            }
        }
        instanceToTransform = constructor.callBy(params)
    }
    return operation(instanceToTransform)
}

fun Node.isBefore(other: Node): Boolean = position!!.start.isBefore(other.position!!.start)

fun Point.isBefore(other: Point): Boolean = line < other.line || (line == other.line && column < other.column)

data class Point(val line: Int, val column: Int) {
    override fun toString() = "Line $line, Column $column"
}

data class Position(val start: Point, val end: Point)

fun pos(startLine: Int, startCol: Int, endLine: Int, endCol: Int) =
    Position(
        Point(
            startLine,
            startCol
        ), Point(endLine, endCol)
    )

data class PseudoFile(val statements: List<Statement>, override val position: Position? = null) :
    Node

interface Statement : Node
interface Expression : Node

//
// Expressions
//

data class FunctionCallExpression(
    val callee: Expression,
    val arguments: List<Expression>,
    override val position: Position? = null
) : Expression

data class MemberExpression(val parent: Expression, val member: String, override val position: Position? = null) :
    Expression

data class NotExpression(val expression: Expression, override val position: Position? = null) :
    Expression

data class ComparisonOrEqualToExpression(
    val left: Expression, val right: Expression, val operator: String,
    override val position: Position? = null
) :
    Expression

data class AndExpression(val left: Expression, val right: Expression, override val position: Position? = null) :
    Expression

data class OrExpression(val left: Expression, val right: Expression, override val position: Position? = null) :
    Expression

data class VarReference(val varName: String, override val position: Position? = null) :
    Expression

data class IntLit(val value: String, override val position: Position? = null) :
    Expression

data class DecLit(val value: String, override val position: Position? = null) :
    Expression

data class BooleanLit(val value: String, override val position: Position? = null) :
    Expression

data class StringLit(val value: String, override val position: Position? = null) :
    Expression

data class NullLit(override val position: Position? = null) :
    Expression

data class ArrayLit(val items: List<Expression>, override val position: Position? = null) :
    Expression

data class NewObject(val clazz: String, val params: List<Expression>, override val position: Position? = null) :
    Expression

data class SuperExpression(override val position: Position? = null) :
    Expression

//
// Statements
//

data class ArrayDeclaration(
    val name: String,
    val dimensions: List<Expression>,
    val global: Boolean,
    override val position: Position? = null
) :
    Statement

data class GlobalAssignment(val identifier: String, val right: Expression, override val position: Position? = null) :
    Statement

data class IdentifierAssignment(val left: String, val right: Expression, override val position: Position? = null) :
    Statement

data class MemberAssignment(
    val left: Expression,
    val member: String,
    val right: Expression,
    override val position: Position? = null
) :
    Statement

data class FunctionCallStatement(
    val callee: Expression,
    val arguments: List<Expression>,
    override val position: Position? = null
) : Statement

data class IfStatement(
    val condition: Expression,
    val body: List<Statement>,
    val elseIfClauses: List<ElseIfClause>,
    val elseClause: ElseClause?,
    override val position: Position? = null
) :
    Statement

data class ElseIfClause(val condition: Expression, val body: List<Statement>, override val position: Position? = null) :
    Statement

data class ElseClause(val body: List<Statement>, override val position: Position? = null) :
    Statement

data class SwitchStatement(
    val subject: Expression,
    val cases: List<CaseClause>,
    val default: DefaultClause?,
    override val position: Position? = null
) :
    Statement

data class CaseClause(val value: Expression, val body: List<Statement>, override val position: Position? = null) :
    Statement

data class DefaultClause(val body: List<Statement>, override val position: Position? = null) :
    Statement

data class WhileStatement(
    val condition: Expression,
    val body: List<Statement>,
    override val position: Position? = null
) :
    Statement

data class DoUntilStatement(
    val condition: Expression,
    val body: List<Statement>,
    override val position: Position? = null
) :
    Statement

data class ForStatement(
    val loopVar: String,
    val initVal: Expression,
    val endVal: Expression,
    val body: List<Statement>,
    override val position: Position? = null
) :
    Statement

data class FunctionDeclarationStatement(
    val name: String, val params: List<String>, val body: List<Statement>,
    override val position: Position? = null
) : Statement

data class ReturnStatement(val expression: Expression?, override val position: Position? = null) :
    Statement

data class BreakStatement(override val position: Position? = null) :
    Statement

data class ContinueStatement(override val position: Position? = null) :
    Statement

data class ClassDeclaration(
    val name: String,
    val superClass: String?,
    val fields: List<Field>, val methods: List<Method>, val constructor: Method,
    override val position: Position?
) :
    Statement

data class Field(val visibility: Visibility, val name: String)

data class Method(val visibility: Visibility, val name: String, val params: List<String>, val body: List<Statement>)

data class SuperConstructorCall(val arguments: List<Expression>, override val position: Position? = null) :
    Statement

//
// Synthetic
//

data class SlotLoadExpression(val slot: Int, override val position: Position? = null) :
    Expression