#!/bin/zsh
cd /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c
./compile-builtin.sh
cp ./builtin-func.ll /tmp
cd /tmp || exit
# llc --march=riscv32 --mattr=+m ./builtin-func.ll -o ./builtin-func.s
rm a.out
# echo "Now ready to link!"
if [[ $1 == "o" ]]
then 
		target=optim.ll
		echo "Copmiler optim file"
else
		target=Basic1.ll
fi
llvm-link ${target} builtin-func.ll -o final.ll -S
clang final.ll
# llc --march=riscv32 --mattr=+m Basic1.ll -o /dev/stdout
echo "LLVM generated with no error!"
