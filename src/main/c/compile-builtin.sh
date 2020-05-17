#!/bin/bash
clang -S -emit-llvm builtin-func.c -o builtin-func.ll
clang --target=riscv32 -march=rv32ima -S t.c -o ./builtin.s
