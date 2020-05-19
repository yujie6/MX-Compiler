#!/bin/zsh
# shellcheck disable=SC2164
cd /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c
./compile-builtin.sh
cp ./builtin.s /tmp
cd /tmp || exit
echo "Preparation done"
mv ./test.out ans.txt
ravel --oj-mode
echo >> test.in
echo >> ans.txt
diff -B -b ./ans.txt ./test.out