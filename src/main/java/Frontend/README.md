# Frontend structure

[o] Use GlobalScopeBuilder to develop global scope, which 
is a collection of `hashmap<String, Entity>` 
for global variable, function and class.   

[x] Use LocalScopeBuilder to traverse again, and check the
semantic errors by local scope, using 
something like a stack of scope.

[x] Build symbol table and implement type checking. 