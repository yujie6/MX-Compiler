# Frontend structure

* `ASTBuilder` build abstract syntax tree from parse tree
created by antler
* `GlobalScopeBuilder`, build global scope, store 
global variables, class and functions.
* `SemanticChekcer`, maintain localScope to do 
type checking and something other semantic checks.
