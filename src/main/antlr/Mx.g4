//adapted from official java grammar file
grammar Mx;

@header {
import com.antlr.MxVisitor;
import com.antlr.MxListener;
import com.antlr.MxParser;
}

import LexerRules;


//parser part
mxProgram: (declaration)* EOF;
declaration: classDeclaration
           | funcDeclaration
           | variableDeclaration ';'
           ;

classDeclaration: CLASS IDENTIFIER classBody;
classBody: '{' classBodyDeclaration* '}';
classBodyDeclaration:variableDeclaration ';'
                    | methodDeclaration;
methodDeclaration: (typeTypeOrVoid)? IDENTIFIER parameters block;


variableDeclaration:typeType variableDecorator(',' variableDecorator)* ;
variableDecorator: IDENTIFIER ( '=' expression)?;
typeType: (classType | primitiveType) ('[' ']')*;
typeTypeOrVoid: typeType
                | VOID;
classType:IDENTIFIER;
primitiveType:BOOL
              | INT
              | STRING;


funcDeclaration:typeTypeOrVoid IDENTIFIER parameters block;
parameters: '(' parameterList? ')';
parameterList: parameter (',' parameter)*;
parameter: typeType IDENTIFIER;
block:'{' blockStatement '}' ;
blockStatement
    : statement *
    ;
//oneLineStatement: variableDeclaration';' | statement;

statement
    : blockLabel=block                                  #blockStmt
    | IF '('expression')' statement (ELSE statement)?   #ifStmt
    | FOR '(' forControl ')' statement                  #forStmt
    | WHILE '('expression')' statement                  #whileStmt
    | RETURN expression? ';'                            #returnStmt
    | BREAK ';'                                         #breakStmt
    | CONTINUE ';'                                      #continueStmt
    | ';'                                               #semiStmt
    | statementExpression=expression ';'                #exprStmt
    | variableDeclaration ';'                           #variableDeclStmt
    ;

expression
    : primary                                    #primaryExpr
    | expression bop='.' IDENTIFIER              #memberExpr
    | expression '[' expression ']'              #arrayExpr
    |  expression '(' expressionList? ')'        #methodCallExpr
    | NEW creator                                #newExpr
    | expression postfix=('++' | '--')           #postfixExpr
    | prefix=('+'|'-'|'++'|'--') expression      #prefixExpr
    | prefix=('~'|'!') expression                #prefixExpr
    | expression bop=('*'|'/'|'%') expression    #binaryOpExpr
    | expression bop=('+'|'-') expression        #binaryOpExpr
    | expression bop=('<<' | '>>>' | '>>') expression #binaryOpExpr
    | expression bop=('<=' | '>=' | '>' | '<') expression #binaryOpExpr
    | expression bop=('==' | '!=') expression   #binaryOpExpr
    | expression bop='&' expression             #binaryOpExpr
    | expression bop='^' expression             #binaryOpExpr
    | expression bop='|' expression             #binaryOpExpr
    | expression bop='&&' expression            #binaryOpExpr
    | expression bop='||' expression            #binaryOpExpr
    | <assoc=right> expression bop='=' expression   #binaryOpExpr

;

forControl
    : forinit=expression? ';' forcond=expression? ';' forUpdate=expression?
    ;


expressionList
    : expression (',' expression)*
    ;

primary
    : '(' expression ')' #parenthesizedExpr
    | THIS               #thisExpr
    | literal            #literalExpr
    | IDENTIFIER         #nameExpr
    ;
literal
    : DECIMAL_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    | NULL_LITERAL
    ;

creator
    : (classType |primitiveType) ('[' expression']')+ ('['']')* #arrayCreator
      | (classType |primitiveType) ('(' ')' )?  #constructorCreator
    ;