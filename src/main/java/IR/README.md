# Learning LLVM
## Instruction
* Binary Operations
    * `nuw` and `nsw` stand for 
    “No Unsigned Wrap” and “No Signed Wrap”, respectively.
    They are used to generate *poison value*.

* Memory Access and Addressing Operations
    * `alloca`: The `alloca` instruction allocates memory on the stack frame of the currently executing function, 
    automatically push and pop.
    * `load`:  Read from memory
    * `store`: Write to memory
    * Examples:
        ```llvm
        %ptr = alloca i32             ; yields i32*:ptr
        store i32 3, i32* %ptr        ; yields void
        %val = load i32, i32* %ptr    ; yields i32:val = i32 3
        ```
  
* Branch Instruction
    * Format: 
        * `br i1 <cond>, label <iftrue>, label <iffalse>`  
        * `br label <dest>` 
        * `switch <intty> <value>, label <defaultdest> [ <intty> <val>, label <dest> ... ]`
    * Example:
        ```
        Test:
          %cond = icmp eq i32 %a, %b
          br i1 %cond, label %IfEqual, label %IfUnequal
        IfEqual:
          ret i32 1
        IfUnequal:
          ret i32 0
        ``` 
      
* Phi instruction
    * Format: 
    `<result> = phi [fast-math-flags] <ty> [ <val0>, <label0>], ...`
    * To implement SSA, we use phi node to assign variable 
    based on previous block.
* [**GetElementPtr**](https://llvm.org/docs/GetElementPtr.html)
    * Compute pointers: `getelementptr (TY, CSTPTR, IDX0, IDX1, ...)`
    * Example: 
        ``` 
        getelementptr %struct.my_struct* %P, i32 1, i32 0
      ; used to compute pointer point to P[1]'s 0 th element
        getelementptr i32 %P, i32 1
      ; used to compute address of P[1]    
        ```
* Other Constant Expressions
    * `icmp COND (VAL1, VAL2)`
    * `bitcast (CST to TYPE)`
        * The ‘bitcast’ instruction converts value to type ty2 without changing any bits.
    * `sext` (sign extension)
        * `<result> = sext <ty> <value> to <ty2> `
    * `select (COND, VAL1, VAL2)`
* Other Aggregate Operations
    * `extractvalue` Instruction
        * Examples:
            ```
            <result> = extractvalue {i32, float} %agg, 0    ; yields i32
            ```
    * `insertvalue` Instruction
        * Examples:
            ```
            %agg1 = insertvalue {i32, float} undef, i32 1, 0              ; yields {i32 1, float undef}
            %agg2 = insertvalue {i32, float} %agg1, float %val, 1         ; yields {i32 1, float %val}
            %agg3 = insertvalue {i32, {float}} undef, float %val, 1, 0    ; yields {i32 undef, {float %val}}
            ```
 

## Type system
* Void Type
    * void 
* Function Type: 
    * Format: `<returntype> (<parameter list>)`
    * Example:
         ```
        call i32 (i8*, ...) @printf(
      i8* getelementptr inbounds ([7 x i8], [7 x i8]* @.str, 
      i64 0, i64 0))
        ```
* Structure Type
    * Represent a collection of data members together in 
    memory.
    * Structures in memory are accessed using 
    `load` and `store` by getting a pointer to 
    **a field with the `getelementptr`**. 
    Structures in registers are accessed using 
    the `extractvalue` and `insertvalue` instructions.
        ```
        %T1 = type { <type list> }     ;normal struct type
        %T2 = type <{ <type list> }>   ;packed ones (align 1)
        ```

* Label Type
    * 
* Array Type
* Vector Type
* Pointer Type

## Intrinsic Functions
* llvm.umul.with.overflow ...
* useful but we don't need this