# MX-Compiler
A compiler from scratch for Mx*(a c-and-java-like language)
to riscv32.

To run the project
```bash
./gradlew installDist
./build/install/Compiler/bin/Compiler inputfile
```
More command line options:

    usage: MX-Compiler
     -g,--debug <arg>    set level of debug information
     -h,--help           print this message
     -i,--input <arg>    input file path
     -o,--output <arg>   output file
    
    debug option	description
    -g 0		no debug information
    -g 1		only warning information
    -g 2		with minimal information
    -g 3		with detail information

### Current Objectives
* Dead code elimination
* Function inline

