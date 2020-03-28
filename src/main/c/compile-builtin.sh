#!/bin/bash
clang -S -emit-llvm builtin-func.c -o builtin-func.ll
