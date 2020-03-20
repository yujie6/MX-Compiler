#!/bin/zsh
cp /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c/builtin-func.ll /tmp
cd /tmp || exit
rm a.out
echo "Now ready to link!"
llvm-link Basic1.ll builtin-func.ll -o final.ll -S
clang final.ll
echo "LLVM generated with no error!"