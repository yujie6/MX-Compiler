package IR;

import AST.BlockNode;
import IR.Instructions.AllocaInst;
import IR.Instructions.LoadInst;
import IR.Instructions.ReturnInst;
import IR.Types.FunctionType;
import IR.Types.IRBaseType;
import Optim.FuncAnalysis.LoopAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A function definition contains a list of basic blocks, forming the CFG (Control Flow Graph) for the function.
 * Each basic block may optionally start with a label (giving the basic block a symbol table entry),
 * contains a list of instructions, and ends with a terminator instruction (such as a branch or function return).
 * <p>
 * The first basic block in a function is special in two ways: it is immediately executed on entrance to the function,
 * and it is not allowed to have predecessor basic blocks (i.e. there can not be any branches to the entry block
 * of a function). Because the block can have no predecessors, it also cannot have any PHI nodes.
 */
public class Function extends Value {

    private String Identifier;
    private FunctionType functionType;
    private ArrayList<Argument> ParameterList;
    private ArrayList<BasicBlock> BlockList;
    private BasicBlock HeadBlock, TailBlock, RetBlock;
    private Value RetValue, thisExpr;
    private ValueSymbolTable varSymTab;
    private boolean isExternal;
    private LoopAnalysis LA;

    public ArrayList<BasicBlock> getBlockList() {
        return BlockList;
    }

    public Function(String id, IRBaseType returnType, ArrayList<Argument> parameterList, boolean isExternal) {
        super(ValueType.FUNCTION);
        this.Identifier = id;
        this.ParameterList = parameterList;
        varSymTab = new ValueSymbolTable();
        this.BlockList = new ArrayList<>();
        ArrayList<IRBaseType> argTypeList = new ArrayList<>();
        for (Argument argument : parameterList) {
            argument.setParent(this);
            argTypeList.add(argument.getArgType());
            varSymTab.put(argument.getName(), argument);
        }
        functionType = new FunctionType(returnType, argTypeList);
        functionType.setIdentifiler(id);
        HeadBlock = null;
        TailBlock = null;
        RetBlock = null;
        this.isExternal = isExternal;
    }

    @Override
    public String toString() {
        // this function only print declare, instead of the body
        // something like `declare i32 @printf(i8*, ...) #2`
        StringBuilder ans = new StringBuilder("declare ");
        ans.append(functionType.getReturnType().toString()).append(" @");
        ans.append(Identifier).append("(");
        if (ParameterList.size() > 0) {
            ans.append(ParameterList.get(0).getArgType().toString());
            for (int i = 1; i < ParameterList.size(); i++) {
                ans.append(", ").append(ParameterList.get(i).getArgType().toString());
            }
        }
//        else {
//            ans.append("...");
//        }
        ans.append(")");
        return ans.toString();

    }

    public String getIdentifier() {
        return Identifier;
    }

    public ArrayList<Argument> getParameterList() {
        return ParameterList;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }


    public BasicBlock getHeadBlock() {
        return HeadBlock;
    }

    public BasicBlock getTailBlock() {
        return TailBlock;
    }

    public ValueSymbolTable getVarSymTab() {
        return varSymTab;
    }

    public boolean isEmpty() {
        return HeadBlock == null && TailBlock == null;
    }

    public void AddBlockAtTail(BasicBlock basicBlock) {
        if (HeadBlock == null) {
            HeadBlock = basicBlock;
        }
        this.BlockList.add(basicBlock);
        TailBlock = basicBlock;
    }

    public void AddBlockAfter(BasicBlock fa, BasicBlock basicBlock) {
        int index = BlockList.indexOf(fa);
        BlockList.add(index + 1, basicBlock);
    }

    public void AddBlockBefore(BasicBlock ch, BasicBlock basicBlock) {
        int index = BlockList.indexOf(ch);
        BlockList.add(index, basicBlock);
    }

    public void removeBlock(BasicBlock BB) {
        BlockList.remove(BB);
    }

    public void initialize() {
        BasicBlock head = new BasicBlock(this, "head_block");
        AddBlockAtTail(head);
        RetBlock = new BasicBlock(this, "ret_block");
        // AddBlockAtTail(RetBlock);
        IRBaseType RetType = functionType.getReturnType();
        if (RetType.getBaseTypeName() == IRBaseType.TypeID.VoidTyID) {
            RetBlock.AddInstAtTail(new ReturnInst(RetBlock, Module.VOID, null));
        } else {
            // Optimization here
            AllocaInst RetAddr = new AllocaInst(HeadBlock, RetType);
            RetValue = RetAddr;
            HeadBlock.AddInstAtTail(RetAddr);
            LoadInst LoadedValue = new LoadInst(RetBlock, RetType, RetAddr);
            RetBlock.AddInstAtTail(LoadedValue);
            RetBlock.AddInstAtTail(new ReturnInst(RetBlock, RetType, LoadedValue));
        }
    }

    public void setLA(LoopAnalysis LA) {
        this.LA = LA;
    }

    public LoopAnalysis getLA() {
        return LA;
    }

    public BasicBlock getRetBlock() {
        return RetBlock;
    }

    public Value getRetValue() {
        return RetValue;
    }

    public void setRetValue(Value retValue) {
        RetValue = retValue;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public Value getThisExpr() {
        return thisExpr;
    }

    public void setThisExpr(Value thisExpr) {
        this.thisExpr = thisExpr;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return null;
    }
}
