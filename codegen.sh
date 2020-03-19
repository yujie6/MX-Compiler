#!/bin/zsh
cp /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c/builtin-func.ll /tmp
cd /tmp || exit
echo "Now ready to link!"
llvm-link Basic1.ll builtin-func.ll -o final.ll -S
clang final.ll