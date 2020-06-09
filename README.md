# MX-Compiler
A compiler from scratch for Mx*(a c-and-java-like language)
to riscv32. Able to generate correct assembly file.

To run the project
```bash
./gradlew installDist
./build/install/Compiler/bin/Compiler inputfile
```
To run codegen tests with llvm or assembly
```bash
./gradlew test --tests LLVMTest
./gradlew test --tests CodegenTest
```
More command line options:

    usage: MX-Compiler
     -g,--debug <arg>    set level of debug information
     -h,--help           print this message
     -i,--input <arg>    input file path
     -o,--output <arg>   output file (rv32 Assembly)
    
    debug option	description
    -g 0		no debug information
    -g 1		only warning information
    -g 2		with minimal information
    -g 3		with detail information

### Current Objectives
* Optimizations
    * Graph simplification (edge splitter & preheader)
    * Inst Combine
    * Strength Reduction
    * Induction Variable Recognition
* Some non-trivial optimizations
* Improve code quality in code generation and IR

