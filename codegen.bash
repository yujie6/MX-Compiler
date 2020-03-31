#!/bin/zsh
cd /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c
./compile-builtin.sh
cp ./builtin-func.ll /tmp
cd /tmp || exit
# llc --march=riscv32 --mattr=+m ./builtin-func.ll -o ./builtin-func.s
rm a.out
# echo "Now ready to link!"
llvm-link Basic1.ll builtin-func.ll -o final.ll -S
clang final.ll
# llc --march=riscv32 --mattr=+m Basic1.ll -o /dev/stdout
echo "LLVM generated with no error!"
