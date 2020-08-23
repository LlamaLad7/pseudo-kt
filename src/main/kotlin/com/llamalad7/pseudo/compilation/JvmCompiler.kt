package com.llamalad7.pseudo.compilation

import codes.som.anthony.koffee.ClassAssembly
import codes.som.anthony.koffee.MethodAssembly
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.insns.sugar.construct
import codes.som.anthony.koffee.insns.sugar.invokestatic
import codes.som.anthony.koffee.insns.sugar.invokevirtual
import codes.som.anthony.koffee.insns.sugar.push_int
import codes.som.anthony.koffee.modifiers.public
import com.llamalad7.pseudo.ast.*
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.runtime.scope.*
import com.llamalad7.pseudo.utils.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LabelNode
import java.io.PrintStream
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.math.BigDecimal
import java.math.BigInteger

class JvmCompiler(private val mainClassName: String) {
    val classes = mutableListOf<ClassAssembly>()

    private var currentClassConstant = Type.getObjectType(mainClassName)

    private var currentClassContext: String? = null

    private var currentScopeType = ScopeType.TOP_LEVEL

    private val String.jvmClassName get() = "com/llamalad7/pseudo/user/$this"

    private val scopeIndex = 1

    private val thisIndexForCalls = 2

    private var lowestFreeIndex = thisIndexForCalls + 3

    private var breakLabel: LabelNode? = null

    private var continueLabel: LabelNode? = null

    fun accept(root: PseudoFile) {
        classes.add(newClassAssembly(public, mainClassName, Opcodes.V1_8))
        with(classes[0]) {
            method(public + static, "main", void, Array<String>::class) {
                invokestatic(System::nanoTime)
                lstore(3)
                root.statements.forEach {
                    add(it)
                }
                getstatic(System::class, "out", PrintStream::class)
                construct(StringBuilder::class, void)
                ldc("Program execution took ")
                invokevirtual(StringBuilder::class, "append", StringBuilder::class, String::class)
                invokestatic(System::nanoTime)
                lload(3)
                lsub
                push_int(1000000)
                i2l
                ldiv
                invokevirtual(StringBuilder::class, "append", StringBuilder::class, long)
                ldc(" milliseconds")
                invokevirtual(StringBuilder::class, "append", StringBuilder::class, String::class)
                invokevirtual(StringBuilder::toString)
                invokevirtual(PrintStream::class, "println", void, String::class)
                `return`
            }
        }
    }

    private fun MethodAssembly.loadScope() {
        when (currentScopeType) {
            ScopeType.TOP_LEVEL -> {
                getstatic(TopLevelScope::class, "INSTANCE", TopLevelScope::class)
            }
            ScopeType.FUNCTION -> {
                aload(scopeIndex)
            }
            ScopeType.CLASS -> TODO("not implemented")
        }
    }

    private fun MethodAssembly.add(statement: Statement) {
        when (statement) {
            is ArrayDeclaration -> add(statement)
            is GlobalAssignment -> add(statement)
            is IdentifierAssignment -> add(statement)
            is MemberAssignment -> add(statement)
            is FunctionCallStatement -> add(statement)
            is IfStatement -> add(statement)
            is SwitchStatement -> add(statement)
            is WhileStatement -> add(statement)
            is DoUntilStatement -> add(statement)
            is ForStatement -> add(statement)
            is FunctionDeclarationStatement -> add(statement)
            is ReturnStatement -> when {
                currentScopeType == ScopeType.TOP_LEVEL -> `return`
                statement.expression == null -> {
                    invokestaticgetter(ObjectCache::nullInstance)
                    areturn
                }
                else -> {
                    add(statement.expression)
                    areturn
                }
            }
            is BreakStatement -> goto(
                breakLabel
                    ?: error("Line ${statement.position?.start?.line}: Attempt to break when there is no active loop")
            )
            is ContinueStatement -> goto(
                continueLabel
                    ?: error("Line ${statement.position?.start?.line}: Attempt to continue when there is no active loop")
            )
        }
    }

    private fun MethodAssembly.add(arrayDeclaration: ArrayDeclaration) {
        if (arrayDeclaration.global) {
            if (currentScopeType != ScopeType.TOP_LEVEL) error("Line ${arrayDeclaration.position?.start?.line}: Global variables must be declared in the com.llamalad7.pseudo.main program")
            getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class)
        } else {
            loadScope()
        }
        ldc(arrayDeclaration.name)
        push_int(arrayDeclaration.dimensions.size)
        anewarray(BaseObject::class)
        for ((index, dimension) in arrayDeclaration.dimensions.withIndex()) {
            dup
            push_int(index)
            add(dimension)
            aastore
        }
        invokejvmstatic(ArrayObject.Companion::makeWithDimensions)
        ldc(currentClassConstant)
        invokevirtual(Scope::set)
    }

    private fun MethodAssembly.add(assignment: GlobalAssignment) {
        if (currentScopeType != ScopeType.TOP_LEVEL) error("Line ${assignment.position?.start?.line}: Global variables must be declared in the com.llamalad7.pseudo.main program")
        getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class)
        ldc(assignment.identifier)
        add(assignment.right)
        ldc(currentClassConstant)
        invokevirtual(Scope::set)
    }

    private fun MethodAssembly.add(assignment: IdentifierAssignment) {
        loadScope()
        ldc(assignment.left)
        add(assignment.right)
        ldc(currentClassConstant)
        invokevirtual(Scope::set)
    }

    private fun MethodAssembly.add(assignment: MemberAssignment) {
        add(assignment.left)
        ldc(assignment.member)
        add(assignment.right)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::setMember)
    }

    private fun MethodAssembly.add(functionCallStatement: FunctionCallStatement) {
        addFunctionCall(functionCallStatement.callee, functionCallStatement.arguments)
        pop
    }

    private fun MethodAssembly.add(ifStatement: IfStatement) {
        val end = LabelNode()
        addIfClause(ifStatement.condition, ifStatement.body, end)
        ifStatement.elseIfClauses.forEach {
            addIfClause(it.condition, it.body, end)
        }
        ifStatement.elseClause?.body?.forEach { add(it) }
        instructions.add(end)
    }

    private fun MethodAssembly.addIfClause(condition: Expression, body: List<Statement>, endLabel: LabelNode) {
        val after = LabelNode()
        add(condition)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifeq(after)
        body.forEach { add(it) }
        goto(endLabel)
        instructions.add(after)
    }

    private fun MethodAssembly.add(switchStatement: SwitchStatement) {
        val slot = lowestFreeIndex
        lowestFreeIndex++
        add(switchStatement.subject)
        astore(slot)
        val end = LabelNode()
        switchStatement.cases.forEach {
            addIfClause(
                FunctionCallExpression(
                    MemberExpression(
                        SlotLoadExpression(slot),
                        "==".operatorName
                    ),
                    listOf(it.value)
                ),
                it.body,
                end
            )
        }
        switchStatement.default?.body?.forEach { add(it) }
        instructions.add(end)
        lowestFreeIndex--
    }

    private fun MethodAssembly.add(whileStatement: WhileStatement) {
        val (oldBreak, oldContinue) = breakLabel to continueLabel

        val start = LabelNode()
        val end = LabelNode()
        continueLabel = start
        breakLabel = end

        instructions.add(start)
        add(whileStatement.condition)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifeq(end)
        whileStatement.body.forEach { add(it) }
        goto(start)
        instructions.add(end)

        breakLabel = oldBreak
        continueLabel = oldContinue
    }

    private fun MethodAssembly.add(doUntilStatement: DoUntilStatement) {
        val (oldBreak, oldContinue) = breakLabel to continueLabel

        val start = LabelNode()
        continueLabel = start
        val end = LabelNode()
        breakLabel = end
        instructions.add(start)
        doUntilStatement.body.forEach { add(it) }
        add(doUntilStatement.condition)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifeq(start)
        instructions.add(end)

        breakLabel = oldBreak
        continueLabel = oldContinue
    }

    private fun MethodAssembly.add(forStatement: ForStatement) {
        val (oldBreak, oldContinue) = breakLabel to continueLabel

        val slot = lowestFreeIndex
        lowestFreeIndex++
        val start = LabelNode()
        continueLabel = start
        val end = LabelNode()
        breakLabel = end
        add(forStatement.endVal)
        astore(slot)
        add(
            IdentifierAssignment(
                forStatement.loopVar,
                forStatement.initVal
            )
        )
        instructions.add(start)
        add(
            FunctionCallExpression(
                MemberExpression(
                    VarReference(forStatement.loopVar),
                    ">".operatorName
                ),
                listOf(SlotLoadExpression(slot))
            )
        )
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifne(end)
        forStatement.body.forEach { add(it) }
        add(
            IdentifierAssignment(
                forStatement.loopVar,
                FunctionCallExpression(
                    MemberExpression(
                        VarReference(forStatement.loopVar),
                        "+".operatorName
                    ),
                    listOf(IntLit("1"))
                )
            )
        )
        goto(start)
        instructions.add(end)
        lowestFreeIndex--

        breakLabel = oldBreak
        continueLabel = oldContinue
    }

    private fun MethodAssembly.add(functionDeclarationStatement: FunctionDeclarationStatement) {
        if (currentClassContext == null) {
            val oldScopeType = currentScopeType
            classes[0].method(
                public + static,
                functionDeclarationStatement.name,
                BaseObject::class,
                Array<BaseObject>::class
            ) {
                currentScopeType = ScopeType.FUNCTION
                construct(FunctionScope::class, void, Array<BaseObject>::class, Array<String>::class, Scope::class) {
                    aload_0
                    push_int(functionDeclarationStatement.params.size)
                    anewarray(String::class)
                    for ((index, param) in functionDeclarationStatement.params.withIndex()) {
                        dup
                        push_int(index)
                        ldc(param)
                        aastore
                    }
                    getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class)
                }
                astore(scopeIndex)
                functionDeclarationStatement.body.forEach {
                    add(it)
                }
                invokestaticgetter(ObjectCache::nullInstance)
                areturn
                currentScopeType = oldScopeType
            }
            getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class)
            ldc(functionDeclarationStatement.name)
            construct(FunctionObject::class, void, Method::class, Int::class) {
                ldc(currentClassConstant)
                ldc(functionDeclarationStatement.name)
                iconst_1
                anewarray(Class::class)
                dup
                iconst_0
                ldc(Type.getType("[Lcom/llamalad7/pseudo/runtime/abstraction/BaseObject;"))
                aastore
                invokevirtual(Class<*>::getMethod)
                push_int(functionDeclarationStatement.params.size)
            }
            ldc(currentClassConstant)
            invokevirtual(Scope::set)
        } else TODO("not implemented yet")
    }

    private fun MethodAssembly.add(expression: Expression) {
        when (expression) {
            is VarReference -> {
                loadScope()
                ldc(expression.varName)
                ldc(currentClassConstant)
                invokevirtual(Scope::get)
            }
            is FunctionCallExpression -> add(expression)
            is MemberExpression -> add(expression)
            is NotExpression -> add(expression)
            is ComparisonOrEqualToExpression -> add(expression)
            is AndExpression -> add(expression)
            is OrExpression -> add(expression)
            is IntLit -> add(expression)
            is DecLit -> add(expression)
            is BooleanLit -> add(expression)
            is StringLit -> add(expression)
            is NullLit -> invokestaticgetter(ObjectCache::nullInstance)

            is SlotLoadExpression -> aload(expression.slot)
        }
    }

    private fun MethodAssembly.add(functionCallExpression: FunctionCallExpression) {
        addFunctionCall(functionCallExpression.callee, functionCallExpression.arguments)
    }

    private fun MethodAssembly.add(memberExpression: MemberExpression) {
        add(memberExpression.parent)
        ldc(memberExpression.member)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::getMember)
    }

    private fun MethodAssembly.add(notExpression: NotExpression) {
        add(notExpression.expression)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        val ifne = LabelNode()
        val end = LabelNode()
        ifne(ifne)
        invokestaticgetter(ObjectCache::trueInstance)
        goto(end)
        instructions.add(ifne)
        invokestaticgetter(ObjectCache::falseInstance)
        instructions.add(end)
    }

    private fun MethodAssembly.add(comparisonOrEqualToExpression: ComparisonOrEqualToExpression) {
        val leftOperator = comparisonOrEqualToExpression.operator[0].toString()
        val firstSlot = lowestFreeIndex
        val secondSlot = firstSlot + 1
        lowestFreeIndex += 2
        add(comparisonOrEqualToExpression.left)
        astore(firstSlot)
        add(comparisonOrEqualToExpression.right)
        astore(secondSlot)
        add(
            OrExpression(
                FunctionCallExpression(
                    MemberExpression(
                        SlotLoadExpression(firstSlot),
                        leftOperator.operatorName
                    ),
                    listOf(
                        SlotLoadExpression(secondSlot)
                    )
                ),
                FunctionCallExpression(
                    MemberExpression(
                        SlotLoadExpression(firstSlot),
                        "==".operatorName
                    ),
                    listOf(
                        SlotLoadExpression(secondSlot)
                    )
                )
            )
        )
        lowestFreeIndex -= 2
    }

    private fun MethodAssembly.add(andExpression: AndExpression) {
        add(andExpression.left)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        val label = LabelNode()
        val end = LabelNode()
        ifeq(label)
        add(andExpression.right)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifeq(label)
        invokestaticgetter(ObjectCache::trueInstance)
        goto(end)
        instructions.add(label)
        invokestaticgetter(ObjectCache::falseInstance)
        instructions.add(end)
    }

    private fun MethodAssembly.add(orExpression: OrExpression) {
        add(orExpression.left)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        val trueLabel = LabelNode()
        val falseLabel = LabelNode()
        val end = LabelNode()
        ifne(trueLabel)
        add(orExpression.right)
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifeq(falseLabel)
        instructions.add(trueLabel)
        invokestaticgetter(ObjectCache::trueInstance)
        goto(end)
        instructions.add(falseLabel)
        invokestaticgetter(ObjectCache::falseInstance)
        instructions.add(end)
    }

    private fun MethodAssembly.add(intLit: IntLit) {
        construct(BigInteger::class, void, String::class) {
            ldc(intLit.value)
        }
        invokejvmstatic(IntObject.Companion::create)
    }

    private fun MethodAssembly.add(decLit: DecLit) {
        construct(BigDecimal::class, void, String::class) {
            ldc(decLit.value)
        }
        invokejvmstatic(FloatObject.Companion::create)
    }

    private fun MethodAssembly.add(booleanLit: BooleanLit) {
        if (booleanLit.value == "false") {
            invokestaticgetter(ObjectCache::falseInstance)
        } else {
            invokestaticgetter(ObjectCache::trueInstance)
        }
    }

    private fun MethodAssembly.add(stringLit: StringLit) {
        ldc(stringLit.value)
        invokejvmstatic(StringObject.Companion::create)
    }

    private fun MethodAssembly.addFunctionCall(callee: Expression, arguments: List<Expression>) {
        when (callee) {
            is MemberExpression -> {
                add(callee.parent)
                dup
                astore(thisIndexForCalls)
                ldc(callee.member)
                ldc(currentClassConstant)
                invokevirtual(BaseObject::getMember)
                push_int(arguments.size + 1)
                anewarray(BaseObject::class)
                dup
                iconst_0
                aload(thisIndexForCalls)
                aastore
                for ((index, arg) in arguments.withIndex()) {
                    dup
                    push_int(index + 1)
                    add(arg)
                    aastore
                }
            }
            else -> {
                add(callee)
                push_int(arguments.size)
                anewarray(BaseObject::class)
                for ((index, arg) in arguments.withIndex()) {
                    dup
                    push_int(index)
                    add(arg)
                    aastore
                }
            }
        }

        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptCall)
    }
}