#!/bin/zsh
# shellcheck disable=SC2164
cd /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c
./compile-builtin.sh
cp ./builtin-func.ll /tmp
cd /tmp || exit
llvm-link Basic1.ll builtin-func.ll -o final.ll -S
clang final.ll
./a.out < test.in > ans.txt
echo >> test.in
echo >> ans.txt
diff -B -b ./ans.txt ./test.out