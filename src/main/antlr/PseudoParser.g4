parser grammar PseudoParser;

options { tokenVocab=PseudoLexer; }
pseudoFile : lines=line* EOF ;
line      : NEWLINE* statement (NEWLINE+ | EOF) ;
statement : assignment                                                      # AssignmentStatement
          | GLOBAL? ARRAY ID LSB nonOptionalCommaSeparatedList RSB          # ArrayDeclaration
          | GLOBAL ID ASSIGN singleExpression                               # GlobalAssignmentStatement
          | SUPER DOT NEW LPAREN commaSeparatedList RPAREN                  # SuperConstructorCall
          | singleExpression LPAREN args=commaSeparatedList RPAREN          # FunctionCallStatement
          | ifStmt                                                          # IfStatement
          | switchStmt                                                      # SwitchStatement
          | whileStmt                                                       # WhileStatement
          | doUntilStmt                                                     # DoUntilStatement
          | forStmt                                                         # ForStatement
          | funcDeclaration                                                 # FunctionDeclaration
          | RETURN singleExpression?                                        # ReturnStatement
          | BREAK                                                           # BreakStatement
          | CONTINUE                                                        # ContinueStatement
          | classDecl                                                       # ClassDeclaration
          ;

assignment : left=singleExpression DOT member=ID ASSIGN right=singleExpression    # MemberDotAssignment
           | left=singleExpression LSB nonOptionalCommaSeparatedList RSB ASSIGN right=singleExpression  # MemberIndexAssignment
           | ID ASSIGN singleExpression                                     # IdentifierAssignment
           ;

ifStmt : IF condition=singleExpression THEN NEWLINE lines=line+ elseIfClause* elseClause? ENDIF;

elseIfClause : ELSEIF condition=singleExpression THEN NEWLINE lines=line+ ;

elseClause : ELSE NEWLINE+ lines=line+ ;

funcDeclaration : FUNCTION name=ID LPAREN identifierList RPAREN NEWLINE line+ ENDFUNCTION ;

switchStmt : SWITCH subject=singleExpression COLON NEWLINE caseClause* defaultClause? ENDSWITCH ;

caseClause : CASE value=singleExpression COLON NEWLINE line+ ;

defaultClause : DEFAULT COLON NEWLINE line+ ;

whileStmt : WHILE condition=singleExpression NEWLINE line+ ENDWHILE ;

doUntilStmt : DO NEWLINE line+ UNTIL condition=singleExpression ;

forStmt : FOR initId=ID ASSIGN init=singleExpression TO until=singleExpression (STEP step=singleExpression)? NEWLINE line+ NEXT nextId=ID ;

classDecl : CLASS name=ID (INHERITS superName=ID)? NEWLINE+ (NEWLINE* classStatement)* NEWLINE* ENDCLASS ;

classStatement : visibility=(PUBLIC | PRIVATE) name=ID # FieldDeclaration
               | visibility=(PUBLIC | PRIVATE) FUNCTION name=ID LPAREN identifierList RPAREN NEWLINE line+ ENDFUNCTION # MethodDeclaration
               | visibility=(PUBLIC | PRIVATE) FUNCTION NEW LPAREN identifierList RPAREN NEWLINE line+ ENDFUNCTION # ConstructorDeclaration ;

singleExpression
    : LPAREN singleExpression RPAREN                                        # ParenthesizedExpression
    | singleExpression LPAREN commaSeparatedList RPAREN                     # FunctionCallExpression
    | singleExpression LSB nonOptionalCommaSeparatedList RSB                # MemberIndexExpression
    | singleExpression DOT member=ID                                        # MemberDotExpression
    | NEW ID LPAREN commaSeparatedList RPAREN                               # NewExpression
    | PLUS singleExpression                                                 # UnaryPlusExpression
    | MINUS singleExpression                                                # UnaryMinusExpression
    | NOT singleExpression                                                  # NotExpression
    | <assoc=right> left=singleExpression EXP right=singleExpression                   # PowerExpression
    | left=singleExpression op=(ASTERISK | DIVISION | DIV | MOD) right=singleExpression# MultiplicativeExpression
    | left=singleExpression op=(PLUS | MINUS) right=singleExpression                   # AdditiveExpression
    | left=singleExpression op=(LT | GT | LE | GE) right=singleExpression              # RelationalExpression
    | left=singleExpression op=(EQ | NE) right=singleExpression                        # EqualityExpression
    | left=singleExpression AND right=singleExpression                                 # LogicalAndExpression
    | left=singleExpression OR right=singleExpression                                  # LogicalOrExpression
    | ID                                                                    # IdentifierExpression
    | SUPER                                                                 # SuperExpression
    | literal                                                               # LiteralExpression
    ;

literal : INTLIT                                                            # IntLiteral
        | DECLIT                                                            # DecLiteral
        | BOOLIT                                                            # BooleanLiteral
        | STRLIT                                                            # StringLiteral
        | NULL                                                              # NullLiteral
        | LCB commaSeparatedList RCB                                        # ArrayLiteral
        | LSB commaSeparatedList RSB                                        # ListLiteral
        ;

commaSeparatedList : (singleExpression COMMA)* (singleExpression)? ;

identifierList : (ID COMMA)* ID? ;

nonOptionalCommaSeparatedList : singleExpression (COMMA singleExpression)* COMMA? ;