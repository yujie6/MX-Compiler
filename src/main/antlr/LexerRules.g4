lexer grammar LexerRules;

//lexer part
//keyword
INT:'int';
BOOL: 'bool';
STRING:'string';
VOID:'void';
IF:'if';
ELSE:'else';
FOR:'for';
WHILE:'while';
BREAK:'break';
CONTINUE:'continue';
RETURN:'return';
NEW:'new';
CLASS:'class';
THIS:'this';


STRING_LITERAL:     '"' (~["\\\r\n]| '\\' ["n\\])* '"';
BOOL_LITERAL:       'true'
            |       'false'
            ;
NULL_LITERAL:       'null';
DECIMAL_LITERAL
    : [1-9] [0-9]*
    | '0'
    ;
IDENTIFIER
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

// Whitespace and comments
WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);