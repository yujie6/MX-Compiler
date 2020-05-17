#!/bin/zsh
./build/install/Compiler/bin/Compiler -g 0 -o ./test.s <&0

function run() {
    ./build/install/Compiler/bin/Compiler -g 0 -o ./test.s
}

function debug() {
    cd /home/yujie6/Documents/Compiler/MX-Compiler/src/main/c
    ./compile-builtin.sh
    cp ./builtin-func.ll /tmp
    cp ./builtin.s /tmp
    cd /tmp || exit
    # llc --march=riscv32 --mattr=+m ./builtin-func.ll -o ./builtin-func.s
    ravel --oj-mode 1>/tmp/ravel.out 2>/tmp/ravel.err
    cat /tmp/ravel.out /tmp/ravel.err | less
    wait
}

function llvm() {
    if [[ $1 == "o" ]]
    then
        target=optim.ll
        echo "Copmiler optim file"
    else
        target=Basic1.ll
    fi
    llvm-link ${target} builtin-func.ll -o final.ll -S
    clang final.ll
}



