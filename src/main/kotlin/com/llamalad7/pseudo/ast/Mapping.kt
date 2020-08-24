package com.llamalad7.pseudo.ast

import com.llamalad7.pseudo.generated.PseudoParser.*
import com.llamalad7.pseudo.runtime.abstraction.Visibility
import com.llamalad7.pseudo.utils.operatorName
import com.llamalad7.pseudo.utils.toMemberCallExpression
import com.llamalad7.pseudo.utils.unescaped
import org.antlr.runtime.Token.EOF
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

fun PseudoFileContext.toAst(considerPosition: Boolean = false): PseudoFile =
    PseudoFile(
        this.line().map { it.statement().toAst(considerPosition) }, toPosition(considerPosition)
    )

fun Token.startPoint() = Point(line, charPositionInLine)
fun Token.endPoint() = Point(
    line,
    charPositionInLine + (if (type == EOF) 0 else text.length)
)

fun ParserRuleContext.toPosition(considerPosition: Boolean): Position? {
    return if (considerPosition) Position(
        start.startPoint(),
        stop.endPoint()
    ) else null
}

fun StatementContext.toAst(considerPosition: Boolean = false): Statement = when (this) {
    is ArrayDeclarationContext -> ArrayDeclaration(
        ID().text,
        nonOptionalCommaSeparatedList().singleExpression().map { it.toAst(considerPosition) },
        GLOBAL() != null,
        toPosition(considerPosition)
    )
    is GlobalAssignmentStatementContext -> GlobalAssignment(
        ID().text,
        singleExpression().toAst(considerPosition),
        toPosition(considerPosition)
    )
    is SuperConstructorCallContext -> SuperConstructorCall(
        commaSeparatedList().singleExpression().map { it.toAst(considerPosition) },
        toPosition(considerPosition)
    )
    is AssignmentStatementContext -> toAst(considerPosition)
    is FunctionCallStatementContext -> toAst(considerPosition)
    is IfStatementContext -> toAst(considerPosition)
    is SwitchStatementContext -> toAst(considerPosition)
    is WhileStatementContext -> toAst(considerPosition)
    is DoUntilStatementContext -> toAst(considerPosition)
    is ForStatementContext -> toAst(considerPosition)
    is FunctionDeclarationContext -> toAst(considerPosition)
    is ReturnStatementContext -> ReturnStatement(
        singleExpression()?.toAst(considerPosition),
        toPosition(considerPosition)
    )
    is BreakStatementContext -> BreakStatement(toPosition(considerPosition))
    is ContinueStatementContext -> ContinueStatement(toPosition(considerPosition))
    is ClassDeclarationContext -> toAst(considerPosition)
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun AssignmentStatementContext.toAst(considerPosition: Boolean = false): Statement =
    when (val assignment = assignment()) {
        is IdentifierAssignmentContext -> IdentifierAssignment(
            assignment.ID().text,
            assignment.singleExpression().toAst(considerPosition),
            toPosition(considerPosition)
        )
        is MemberDotAssignmentContext -> MemberAssignment(
            assignment.left.toAst(considerPosition),
            assignment.member.text,
            assignment.right.toAst(considerPosition),
            toPosition(considerPosition)
        )
        is MemberIndexAssignmentContext -> {
            var expression = assignment.left.toAst(considerPosition)
            val indices = assignment.nonOptionalCommaSeparatedList().singleExpression()
            for (index in 0 until indices.size - 1) {
                expression = FunctionCallExpression(
                    MemberExpression(
                        expression,
                        "[]".operatorName,
                        toPosition(considerPosition)
                    ),
                    listOf(indices[index].toAst(considerPosition)),
                    toPosition(considerPosition)
                )
            }
            FunctionCallStatement(
                MemberExpression(
                    expression,
                    "[]=".operatorName,
                    toPosition(considerPosition)
                ),
                listOf(indices.last().toAst(considerPosition), assignment.right.toAst(considerPosition)),
                toPosition(considerPosition)
            )
        }
        else -> error("oof")
    }

fun FunctionCallStatementContext.toAst(considerPosition: Boolean = false): Statement = FunctionCallStatement(
    singleExpression().toAst(considerPosition),
    args.singleExpression().map { it.toAst(considerPosition) },
    toPosition(considerPosition)
)

fun IfStatementContext.toAst(considerPosition: Boolean = false): Statement = IfStatement(
    ifStmt().condition.toAst(considerPosition),
    ifStmt().line().map { it.statement().toAst(considerPosition) },
    ifStmt().elseIfClause().map { it.toAst(considerPosition) },
    ifStmt().elseClause()?.toAst(considerPosition),
    toPosition(considerPosition)
)

fun ElseIfClauseContext.toAst(considerPosition: Boolean = false) = ElseIfClause(
    condition.toAst(considerPosition),
    line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun ElseClauseContext.toAst(considerPosition: Boolean = false) = ElseClause(
    line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun SwitchStatementContext.toAst(considerPosition: Boolean = false): Statement = SwitchStatement(
    switchStmt().subject.toAst(considerPosition),
    switchStmt().caseClause().map { it.toAst(considerPosition) },
    switchStmt().defaultClause()?.toAst(considerPosition),
    toPosition(considerPosition)
)

fun CaseClauseContext.toAst(considerPosition: Boolean = false) = CaseClause(
    value.toAst(considerPosition),
    line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun DefaultClauseContext.toAst(considerPosition: Boolean = false) = DefaultClause(
    line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun WhileStatementContext.toAst(considerPosition: Boolean = false): Statement = WhileStatement(
    whileStmt().condition.toAst(considerPosition),
    whileStmt().line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun DoUntilStatementContext.toAst(considerPosition: Boolean = false): Statement = DoUntilStatement(
    doUntilStmt().condition.toAst(considerPosition),
    doUntilStmt().line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun ForStatementContext.toAst(considerPosition: Boolean = false): Statement {
    if (forStmt().initId.text != forStmt().nextId.text) error("Next identifier '${forStmt().nextId.text}' on line ${forStmt().nextId.line} doesn't match initial identifier '${forStmt().initId.text}' on line ${forStmt().initId.line}")
    return ForStatement(
        forStmt().initId.text,
        forStmt().init.toAst(considerPosition),
        forStmt().until.toAst(considerPosition),
        forStmt().line().map { it.statement().toAst(considerPosition) },
        toPosition(considerPosition)
    )
}

fun FunctionDeclarationContext.toAst(considerPosition: Boolean = false): Statement = FunctionDeclarationStatement(
    funcDeclaration().name.text,
    funcDeclaration().identifierList().ID().map { it.text },
    funcDeclaration().line().map { it.statement().toAst(considerPosition) },
    toPosition(considerPosition)
)

fun ClassDeclarationContext.toAst(considerPosition: Boolean = false): Statement = ClassDeclaration(
    classDecl().name.text,
    classDecl().superName?.text,
    classDecl().classStatement().filterIsInstance<FieldDeclarationContext>().map { it.toAst() },
    classDecl().classStatement().filterIsInstance<MethodDeclarationContext>().map { it.toAst(considerPosition) },
    classDecl().classStatement().filterIsInstance<ConstructorDeclarationContext>()
        .also { if (it.size > 1) error("Line ${it[1].start.line}: Only one constructor declaration allowed per class") }
        .let {
            if (it.isEmpty()) Method(
                Visibility.PUBLIC,
                "new",
                emptyList(),
                emptyList()
            ) else it[0].toAst(considerPosition)
        },
    toPosition(considerPosition)
)

fun FieldDeclarationContext.toAst() = Field(if (PRIVATE() == null) Visibility.PUBLIC else Visibility.PRIVATE, ID().text)

fun MethodDeclarationContext.toAst(considerPosition: Boolean = false) = Method(
    if (PRIVATE() == null) Visibility.PUBLIC else Visibility.PRIVATE,
    ID().text,
    identifierList().ID().map { it.text },
    line().map { it.statement().toAst(considerPosition) }
)

fun ConstructorDeclarationContext.toAst(considerPosition: Boolean = false) = Method(
    if (PRIVATE() == null) Visibility.PUBLIC else Visibility.PRIVATE,
    NEW().text,
    identifierList().ID().map { it.text },
    line().map { it.statement().toAst(considerPosition) }
)

fun SingleExpressionContext.toAst(considerPosition: Boolean = false): Expression = when (this) {
    is ParenthesizedExpressionContext -> singleExpression().toAst(considerPosition)
    is FunctionCallExpressionContext -> toAst(considerPosition)
    is MemberIndexExpressionContext -> {
        var expression = singleExpression().toAst(considerPosition)
        for (index in nonOptionalCommaSeparatedList().singleExpression().map { it.toAst(considerPosition) }) {
            expression = FunctionCallExpression(
                MemberExpression(
                    expression,
                    "[]".operatorName,
                    toPosition(considerPosition)
                ),
                listOf(index),
                toPosition(considerPosition)
            )
        }
        expression
    }
    is MemberDotExpressionContext -> MemberExpression(
        singleExpression().toAst(considerPosition),
        member.text,
        toPosition(considerPosition)
    )
    is UnaryPlusExpressionContext -> singleExpression().toAst(considerPosition)
    is UnaryMinusExpressionContext -> FunctionCallExpression(
        MemberExpression(
            IntLit("0"),
            "-".operatorName
        ),
        listOf(singleExpression().toAst(considerPosition)),
        toPosition(considerPosition)
    )
    is NotExpressionContext -> NotExpression(
        singleExpression().toAst(considerPosition),
        toPosition(considerPosition)
    )
    is PowerExpressionContext -> toMemberCallExpression(EXP().symbol, left, right, considerPosition)
    is MultiplicativeExpressionContext -> toMemberCallExpression(op, left, right, considerPosition)
    is AdditiveExpressionContext -> toMemberCallExpression(op, left, right, considerPosition)
    is RelationalExpressionContext -> when (op.type) {
        GE, LE -> ComparisonOrEqualToExpression(
            left.toAst(considerPosition),
            right.toAst(considerPosition),
            op.text,
            toPosition(considerPosition)
        )
        else -> toMemberCallExpression(op, left, right, considerPosition)
    }
    is EqualityExpressionContext -> when (op.type) {
        NE -> NotExpression(
            toMemberCallExpression("==", left, right, considerPosition),
            toPosition(considerPosition)
        )
        else -> toMemberCallExpression(op, left, right, considerPosition)
    }
    is LogicalAndExpressionContext -> AndExpression(
        left.toAst(considerPosition),
        right.toAst(considerPosition),
        toPosition(considerPosition)
    )
    is LogicalOrExpressionContext -> OrExpression(
        left.toAst(considerPosition),
        right.toAst(considerPosition),
        toPosition(considerPosition)
    )
    is SuperExpressionContext -> SuperExpression(toPosition(considerPosition))
    is IdentifierExpressionContext -> VarReference(ID().text, toPosition(considerPosition))
    is LiteralExpressionContext -> toAst(considerPosition)
    is NewExpressionContext -> NewObject(
        ID().text,
        commaSeparatedList().singleExpression().map { it.toAst(considerPosition) },
        toPosition(considerPosition)
    )
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun FunctionCallExpressionContext.toAst(considerPosition: Boolean = false): Expression = FunctionCallExpression(
    singleExpression().toAst(considerPosition),
    commaSeparatedList().singleExpression().map { it.toAst(considerPosition) },
    toPosition(considerPosition)
)

fun LiteralExpressionContext.toAst(considerPosition: Boolean = false): Expression = when (val lit = literal()) {
    is IntLiteralContext -> IntLit(lit.text, toPosition(considerPosition))
    is DecLiteralContext -> DecLit(lit.text, toPosition(considerPosition))
    is BooleanLiteralContext -> BooleanLit(lit.text, toPosition(considerPosition))
    is StringLiteralContext -> StringLit(
        lit.text.substring(1, lit.text.length - 1).unescaped,
        toPosition(considerPosition)
    )
    is NullLiteralContext -> NullLit(toPosition(considerPosition))
    else -> error("Unsupported Literal Type")
}