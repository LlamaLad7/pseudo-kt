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
import com.llamalad7.pseudo.runtime.abstraction.Access
import com.llamalad7.pseudo.runtime.abstraction.BaseObject
import com.llamalad7.pseudo.runtime.abstraction.Member
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.runtime.objects.*
import com.llamalad7.pseudo.runtime.scope.*
import com.llamalad7.pseudo.utils.invokejvmstatic
import com.llamalad7.pseudo.utils.invokestaticgetter
import com.llamalad7.pseudo.utils.newClassAssembly
import com.llamalad7.pseudo.utils.operatorName
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LabelNode
import java.io.PrintStream
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.jvm.jvmName

class JvmCompiler(private val mainClassName: String) {
    val classes = mutableListOf<ClassAssembly>()

    private var currentClassConstant =
        Type.getObjectType(mainClassName) // Stores the constant of the current class; used for access checks

    private var currentScopeType = ScopeType.TOP_LEVEL

    private var currentClassMethods = listOf<String>() // A list of methods in the current class; used so we can check if a function call in fact refers to a method call on the current object

    private val String.jvmClassName get() = "com/llamalad7/pseudo/user/$this"

    private val scopeIndex = 1 // Where the current Scope is stored in a method/function

    private val thisIndexForCalls = 2 // Where the `this` context is stored when invoking member functions

    private var lowestFreeIndex =
        thisIndexForCalls + 3 // The lowest free index for use by the compiler (this is incremented and decremented for switches, for loops, etc.)

    private var breakLabel: LabelNode? = null // The label to which we should `goto` if there is a `break`

    private var continueLabel: LabelNode? = null // The label to which we should `goto` if there is a `continue`

    fun accept(root: PseudoFile) {
        classes.add(newClassAssembly(public, mainClassName, Opcodes.V1_8))
        with(classes[0]) {
            method(public + static, "main", void, Array<String>::class) {
                // Used for timing the program:
                invokestatic(System::nanoTime)
                lstore(3)
                // Process each statement in the source file:
                root.statements.forEach {
                    add(it)
                }
                // Used for timing the program:
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

    // Load the current scope onto the stack
    private fun MethodAssembly.loadScope() {
        when (currentScopeType) {
            ScopeType.TOP_LEVEL -> {
                getstatic(TopLevelScope::class, "INSTANCE", TopLevelScope::class)
            }
            ScopeType.FUNCTION, ScopeType.METHOD -> {
                aload(scopeIndex)
            }
        }
    }

    private fun MethodAssembly.add(statement: Statement) {
        when (statement) {
            is ArrayDeclaration -> add(statement)
            is GlobalAssignment -> add(statement)
            is SuperConstructorCall -> add(statement)
            is IdentifierAssignment -> add(statement)
            is MemberAssignment -> add(statement)
            is FunctionCallStatement -> add(statement)
            is IfStatement -> add(statement)
            is SwitchStatement -> add(statement)
            is WhileStatement -> add(statement)
            is DoUntilStatement -> add(statement)
            is ForStatement -> add(statement)
            is FunctionDeclarationStatement -> add(statement)
            is ClassDeclaration -> add(statement)
            is ReturnStatement -> when {
                currentScopeType == ScopeType.TOP_LEVEL -> `return` // The `main` method will return `void`
                statement.expression == null -> {
                    invokestaticgetter(ObjectCache::nullInstance) // All other methods must return a `BaseObject`, and so if no value is specified, the `nullInstance` will be used
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
            if (currentScopeType != ScopeType.TOP_LEVEL) error("Line ${arrayDeclaration.position?.start?.line}: Global variables must be declared in the main program")
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
        if (currentScopeType != ScopeType.TOP_LEVEL) error("Line ${assignment.position?.start?.line}: Global variables must be declared in the main program")
        getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class)
        ldc(assignment.identifier)
        add(assignment.right)
        ldc(currentClassConstant)
        invokevirtual(Scope::set)
    }

    private fun MethodAssembly.add(superConstructorCall: SuperConstructorCall) {
        add(
            FunctionCallStatement(
                MemberExpression(
                    SuperExpression(),
                    "new"
                ),
                superConstructorCall.arguments
            )
        )
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
        pop // All function calls return a value, so if it is ignored (because it is used as a statement), we must pop
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
                        SlotLoadExpression(slot), // A switch statement is really just a chain of `if-else`s with the `==` method
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
        val end = LabelNode()
        breakLabel = end
        val conditionStart = LabelNode()
        continueLabel = conditionStart
        instructions.add(start)
        doUntilStatement.body.forEach { add(it) }
        instructions.add(conditionStart)
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

        val endSlot = lowestFreeIndex
        lowestFreeIndex++
        val incrementSlot = lowestFreeIndex
        lowestFreeIndex++
        val start = LabelNode()
        val end = LabelNode()
        breakLabel = end
        val beforeIncrement = LabelNode()
        continueLabel = beforeIncrement
        add(forStatement.endVal)
        astore(endSlot)
        add(forStatement.increment)
        astore(incrementSlot)
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
                    ">".operatorName // If the loop variable is greater than the loop end, we should stop iterating
                ),
                listOf(SlotLoadExpression(endSlot))
            )
        )
        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptBool)
        ifne(end)
        forStatement.body.forEach { add(it) }
        instructions.add(beforeIncrement)
        add(
            IdentifierAssignment(
                forStatement.loopVar,
                FunctionCallExpression(
                    MemberExpression(
                        VarReference(forStatement.loopVar),
                        "+".operatorName // We should add 1 to the loop variable at the end of each iteration
                    ),
                    listOf(SlotLoadExpression(incrementSlot))
                )
            )
        )
        goto(start)
        instructions.add(end)
        lowestFreeIndex -= 2

        breakLabel = oldBreak
        continueLabel = oldContinue
    }

    private fun MethodAssembly.add(functionDeclarationStatement: FunctionDeclarationStatement) {
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
                for ((index, param) in functionDeclarationStatement.params.withIndex()) { // Construct an array of parameter names
                    dup
                    push_int(index)
                    ldc(param)
                    aastore
                }
                getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class) // This is the parent scope
            }
            astore(scopeIndex)
            functionDeclarationStatement.body.forEach {
                add(it)
            }
            invokestaticgetter(ObjectCache::nullInstance) // Return `nullInstance` in case a value hasn't been returned already; if it has, this will be ignored
            areturn
            currentScopeType = oldScopeType
        }
        getstatic(GlobalScope::class, "INSTANCE", GlobalScope::class) // Add the current function to the global scope
        ldc(functionDeclarationStatement.name)
        construct(FunctionObject::class, void, java.lang.reflect.Method::class, Int::class) {
            ldc(currentClassConstant)
            ldc(functionDeclarationStatement.name)
            iconst_1
            anewarray(Class::class)
            dup
            iconst_0
            ldc(Type.getType("[Lcom/llamalad7/pseudo/runtime/abstraction/BaseObject;")) // All methods take an array of `BaseObject`s to simplify invoking them
            aastore
            invokevirtual(Class<*>::getMethod)
            push_int(functionDeclarationStatement.params.size)
        }
        ldc(currentClassConstant)
        invokevirtual(Scope::set)
    }

    private fun MethodAssembly.add(classDeclaration: ClassDeclaration) {
        if (currentScopeType != ScopeType.TOP_LEVEL) error("Line ${classDeclaration.position?.start?.line}: Classes must be declared in the main program")

        classes.add(
            newClassAssembly(
                public,
                classDeclaration.name.jvmClassName,
                Opcodes.V1_8,
                BaseObject::class.jvmName.replace(".", "/") // All objects must extend `BaseObject`
            )
        )
        with(classes.last()) {
            val oldClassConstant = currentClassConstant
            currentClassConstant = Type.getObjectType(name)
            val oldClassMethods = currentClassMethods
            currentClassMethods = classDeclaration.methods.map { it.name }

            field(private, "instanceMembers", Map::class)

            method(public, "getInstanceMembers", Map::class) {
                aload_0
                getfield(this@with.name, "instanceMembers", Map::class)
                areturn
            }

            field(private + static, "classMembers", Map::class)

            method(public, "getClassMembers", Map::class) {
                getstatic(this@with.name, "classMembers", Map::class)
                areturn
            }

            method(public, "<init>", void) {
                aload_0
                invokespecial(BaseObject::class, "<init>", void)
                aload_0
                construct(ClassScope::class, void, BaseObject::class, Scope::class) {
                    aload_0
                    getstatic(
                        GlobalScope::class,
                        "INSTANCE",
                        GlobalScope::class
                    ) // Construct a new ClassScope, with its parent being the GlobalScope
                }
                invokevirtual(BaseObject::class, "setClassScope", void, ClassScope::class)

                aload_0
                construct(LinkedHashMap::class, void)
                putfield(this@with.name, "instanceMembers", Map::class)

                for (field in classDeclaration.fields) { // Initialize all fields to `nullInstance` in case they are not initialized with another value before they are used
                    aload_0
                    invokevirtual(BaseObject::getInstanceMembers)
                    ldc(field.name)
                    construct(Member::class, void, Visibility::class, Access::class, BaseObject::class) {
                        if (field.visibility == Visibility.PUBLIC) {
                            getstatic(Visibility::class, "PUBLIC", Visibility::class)
                        } else {
                            getstatic(Visibility::class, "PRIVATE", Visibility::class)
                        }
                        getstatic(Access::class, "WRITEABLE", Access::class)
                        invokestaticgetter(ObjectCache::nullInstance)
                    }
                    invokeinterface("java/util/Map", "put", Any::class, Any::class, Any::class)
                }

                if (classDeclaration.superClass != null) {
                    aload_0
                    construct(classDeclaration.superClass.jvmClassName, void)
                    invokevirtual(BaseObject::class, "setParent", void, BaseObject::class)
                }
                `return`
            }

            method(public + static, "<clinit>", void) {
                construct(LinkedHashMap::class, void)
                for (method in classDeclaration.methods + listOf(classDeclaration.constructor)) { // Initialize the `classMembers` map
                    dup
                    ldc(method.name)
                    construct(Member::class, void, Visibility::class, Access::class, BaseObject::class) {
                        if (method.visibility == Visibility.PUBLIC) {
                            getstatic(Visibility::class, "PUBLIC", Visibility::class)
                        } else {
                            getstatic(Visibility::class, "PRIVATE", Visibility::class)
                        }
                        getstatic(Access::class, "READABLE", Access::class)
                        construct(FunctionObject::class, void, java.lang.reflect.Method::class, Int::class) {
                            ldc(currentClassConstant)
                            ldc(method.name)
                            iconst_1
                            anewarray(Class::class)
                            dup
                            iconst_0
                            ldc(Type.getType("[Lcom/llamalad7/pseudo/runtime/abstraction/BaseObject;"))
                            aastore
                            invokevirtual(Class<*>::getMethod)
                            push_int(method.params.size + 1)
                        }
                    }
                    invokeinterface("java/util/Map", "put", Any::class, Any::class, Any::class)
                    pop
                }
                putstatic(this@with.name, "classMembers", Map::class)
                `return`
            }

            for (method in classDeclaration.methods) {
                addClassMethod(method)
            }
            addClassMethod(classDeclaration.constructor)

            currentClassConstant = oldClassConstant
            currentClassMethods = oldClassMethods
        }
    }

    private fun ClassAssembly.addClassMethod(method: Method) {
        val oldScopeType = currentScopeType
        currentScopeType = ScopeType.METHOD
        method(
            public + static, // Even instance members are static, as this simplifies invoking them; they will automatically be passed an instance as their first parameter
            method.name,
            BaseObject::class,
            Type.getType("[Lcom/llamalad7/pseudo/runtime/abstraction/BaseObject;")
        ) {
            construct(FunctionScope::class, void, Array<BaseObject>::class, Array<String>::class, Scope::class) {
                aload_0
                push_int(method.params.size + 1)
                anewarray(String::class)
                dup // Set `this` in the current scope to be the first argument (the instance parameter)
                iconst_0
                ldc("this")
                aastore
                for ((index, param) in method.params.withIndex()) {
                    dup
                    push_int(index + 1) // Account for the first parameter being the object instance
                    ldc(param)
                    aastore
                }
                aload_0
                iconst_0
                aaload
                invokevirtual(
                    BaseObject::class,
                    "getClassScope",
                    ClassScope::class
                ) // The parent scope should be the current `ClassScope`, so we can reference members without needing to use `this`
            }
            astore(scopeIndex)

            method.body.forEach { add(it) }

            invokestaticgetter(ObjectCache::nullInstance)
            areturn
        }
        currentScopeType = oldScopeType
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
            is SuperExpression -> add(expression)
            is IntLit -> add(expression)
            is DecLit -> add(expression)
            is BooleanLit -> add(expression)
            is StringLit -> add(expression)
            is NullLit -> invokestaticgetter(ObjectCache::nullInstance)
            is ArrayLit -> add(expression)
            is ListLit -> add(expression)
            is NewObject -> add(expression)

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
        // Equivalent to `expression ? false : true`
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

    private fun MethodAssembly.add(superExpression: SuperExpression) {
        if (currentScopeType != ScopeType.METHOD) error("Line ${superExpression.position?.start?.line}: 'super' can only be referenced inside a class method")
        aload_0
        iconst_0
        aaload
        invokevirtual(BaseObject::class, "getParent", BaseObject::class)
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

    private fun MethodAssembly.add(arrayLit: ArrayLit) {
        push_int(arrayLit.items.size)
        anewarray(BaseObject::class)
        for ((index, item) in arrayLit.items.withIndex()) {
            dup
            push_int(index)
            add(item)
            aastore
        }
        invokejvmstatic(ArrayObject.Companion::create)
    }

    private fun MethodAssembly.add(listLit: ListLit) {
        push_int(listLit.items.size)
        anewarray(BaseObject::class)
        for ((index, item) in listLit.items.withIndex()) {
            dup
            push_int(index)
            add(item)
            aastore
        }
        invokejvmstatic(ListObject.Companion::create)
    }

    private fun MethodAssembly.add(newObject: NewObject) {
        val slot = lowestFreeIndex
        lowestFreeIndex++
        construct(newObject.clazz.jvmClassName, void)
        dup
        astore(slot)
        add(
            FunctionCallStatement(
                MemberExpression(
                    SlotLoadExpression(slot),
                    "new"
                ),
                newObject.params
            )
        )
        lowestFreeIndex--
    }

    private fun MethodAssembly.addFunctionCall(callee: Expression, arguments: List<Expression>) {
        if (callee is MemberExpression) {
            add(callee.parent)
            dup
            astore(thisIndexForCalls) // We must pass the instance as the first parameter to the method
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
                push_int(index + 1) // Account for the first parameter being the object instance
                add(arg)
                aastore
            }
        } else if (callee is VarReference && currentScopeType == ScopeType.METHOD && callee.varName in currentClassMethods) {
            // Slight hackery to allow for calling instance members in the current class without the use of `this`
            val slot = lowestFreeIndex
            lowestFreeIndex++
            aload_0
            iconst_0
            aaload
            astore(slot)

            addFunctionCall(
                MemberExpression(
                    SlotLoadExpression(slot),
                    callee.varName
                ),
                arguments
            )
            lowestFreeIndex--
            return
        } else {
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

        ldc(currentClassConstant)
        invokevirtual(BaseObject::attemptCall)
    }
}