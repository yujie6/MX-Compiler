# MX-Compiler
A compiler from scratch for Mx*(a c-and-java-like language)

To run the project:
```bash
./gradlew run
```

### Current Objectives
* Debug `basic test [36,42,52,53,57,67]`
* Rebuild GlobalScopeBuilder, perhaps need one more AST traversal 
to scan all the methods for class.
* Catch exception from Parser, e.g. `class int{}` will throw an error
* Improve error handler, collect as much errors as possible.
* Improve grammar file for "for-init"
* Building LLVM IR
