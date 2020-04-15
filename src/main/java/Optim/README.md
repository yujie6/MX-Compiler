# Optimization notes

## 1. Optimizations done
* dominator tree build
* mem2reg
* dead function elimination
* naive dead code elimination 
* aggressive dead code elimination
* common subexpression elimination

## 2. Optimizations to be done 
* loop invariant code motion
* Strength Reduction

## 3. FAQ about optimizations
### 3.1 Is there a need to use dce after mem2reg
    One challenge encountered during evaluation was that the mem2reg pass, which also performs some basic dead code elimination. 
    Thus, immediately prior to running our transformation pass, we always run mem2reg. For our earliest and
    simplest test cases, mem2reg ended up performing the DCE that we had hoped our own pass would.
    However, with the construction of a new and more complex test suite, mem2reg’s partial dead code
    elimination met a limit and the results of our pass became observable.

### 3.2  
