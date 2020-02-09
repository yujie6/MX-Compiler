package Frontend;

import AST.*;
import MxEntity.*;
import Tools.MXError;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GlobalScopeBuilder implements ASTVisitor {
    private Scope globalScope;
    final private Type FunctionType;
    final private Type BoolType;
    final private Type IntType;
    final private Type StringType;
    final private Type VoidType;
    private Logger logger;

    public GlobalScopeBuilder(Logger logger) {
        FunctionType = new Type(BaseType.STYPE_FUNC);
        BoolType = new Type(BaseType.DTYPE_BOOL);
        StringType = new Type(BaseType.DTYPE_STRING);
        IntType = new Type(BaseType.DTYPE_INT);
        VoidType = new Type(BaseType.RTYPE_VOID);
        globalScope = new Scope();
        this.logger = logger;
    }

    private void CheckMainEntry() {

        if (!globalScope.hasFunction("main")) {
            throw new MXError("Program entry \"main\" doesn't exist");
        } else {
            FunctionEntity mainFunc = globalScope.GetFunction("main");
            if (mainFunc.getParaListSize() != 0) {
                throw new MXError("Program entry \"main\" has more than one argument");
            } else if (mainFunc.getReturnType().getBaseType() != BaseType.DTYPE_INT) {
                throw new MXError("Progran entry \"main\" should has int return type");
            }
        }
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    private void PreProcess() {
        // string.length()
        ArrayList<VariableEntity> str_len_para = new ArrayList<>();
        FunctionEntity str_len = new FunctionEntity("length", IntType, "string", true,
                globalScope, str_len_para);
        globalScope.defineFunction(str_len);
        // string.substring(int left, int right)
        ArrayList<VariableEntity> str_substr_para = new ArrayList<>();
        str_substr_para.add(new VariableEntity("left", IntType));
        str_substr_para.add(new VariableEntity("right", IntType));
        FunctionEntity str_substr = new FunctionEntity("substring", StringType, "string", true,
                globalScope, str_substr_para);
        globalScope.defineFunction(str_substr);
        // string.parseInt()
        ArrayList<VariableEntity> str_parseInt_para = new ArrayList<>();
        FunctionEntity str_parseInt = new FunctionEntity("parseInt", IntType, "string", true,
                globalScope, str_parseInt_para);
        globalScope.defineFunction(str_parseInt);
        // string.ord() -> ascii of ith char
        ArrayList<VariableEntity> str_ord_para = new ArrayList<>();
        str_ord_para.add(new VariableEntity("pos", IntType));
        FunctionEntity str_ord = new FunctionEntity("ord", IntType, "string", true,
                globalScope, str_ord_para);
        globalScope.defineFunction(str_ord);
        // string class
        ClassEntity mx_string = new ClassEntity("string", globalScope);
        globalScope.defineClass(mx_string);
        // build-in functions
        // print(string str)
        ArrayList<VariableEntity> print_para = new ArrayList<>();
        print_para.add(new VariableEntity("str", StringType));
        FunctionEntity mx_print = new FunctionEntity("print", VoidType, null, false,
                globalScope, print_para);
        globalScope.defineFunction(mx_print);
        // println(string str) -> end with '/n'
        ArrayList<VariableEntity> println_para = new ArrayList<>();
        println_para.add(new VariableEntity("str", StringType));
        FunctionEntity mx_println = new FunctionEntity("println", VoidType, null, false,
                globalScope, println_para);
        globalScope.defineFunction(mx_println);
        // printlnInt(int n) -> end with '/n'
        ArrayList<VariableEntity> printlnInt_para = new ArrayList<>();
        printlnInt_para.add(new VariableEntity("n", IntType));
        FunctionEntity mx_printlnInt = new FunctionEntity("printlnInt", VoidType, null, false,
                globalScope, printlnInt_para);
        globalScope.defineFunction(mx_printlnInt);
        // getString()
        ArrayList<VariableEntity> getString_para = new ArrayList<>();
        FunctionEntity mx_getString = new FunctionEntity("getString", StringType, null, false,
                globalScope, getString_para);
        globalScope.defineFunction(mx_getString);
        // getInt()
        ArrayList<VariableEntity> getInt_para = new ArrayList<>();
        FunctionEntity mx_getInt = new FunctionEntity("getInt", IntType, null, false,
                globalScope, getInt_para);
        globalScope.defineFunction(mx_getInt);
        // toString(int i)
        ArrayList<VariableEntity> toString_para = new ArrayList<>();
        toString_para.add(new VariableEntity("i", IntType));
        FunctionEntity mx_toString = new FunctionEntity("toString", StringType, null, false,
                globalScope, toString_para);
        globalScope.defineFunction(mx_toString);
        // Array size()
        ArrayList<VariableEntity> size_para = new ArrayList<>();
        FunctionEntity mx_size = new FunctionEntity("size", IntType, "Array", true,
                globalScope, size_para);
        globalScope.defineFunction(mx_size);
        // Array class
        ClassEntity mx_Array = new ClassEntity("Array", globalScope);
        globalScope.defineClass(mx_Array);
    }

    @Override
    public void visit(MxProgramNode node) {
        PreProcess();
        for (DecNode declaration : node.getDecNodeList()) {
            declaration.accept(this);
        }
        CheckMainEntry();
    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity mx_function = new FunctionEntity(globalScope, node, false, null);
        globalScope.defineFunction(mx_function);
    }

    @Override
    public void visit(VariableDecNode node) {
        Type DecType = node.getType();
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            VariableEntity mx_var = new VariableEntity(globalScope, var, DecType);
            globalScope.defineVariable(mx_var);
        }
    }

    @Override
    public void visit(ClassDecNode node) {
        ClassEntity mx_class = new ClassEntity(globalScope, node);
        globalScope.defineClass(mx_class);
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(BlockNode node) {

    }

    @Override
    public void visit(VarDecoratorNode node) {

    }

    @Override
    public void visit(ConstNode node) {

    }

    @Override
    public void visit(ArrayCreatorNode node) {

    }

    @Override
    public void visit(ConstructCreatorNode node) {

    }

    @Override
    public void visit(BinExprNode node) {

    }

    @Override
    public void visit(IDExprNode node) {

    }

    @Override
    public void visit(MemberExprNode node) {

    }

    @Override
    public void visit(ArrayExprNode node) {

    }

    @Override
    public void visit(PrefixExprNode node) {

    }

    @Override
    public void visit(PostfixExprNode node) {

    }

    @Override
    public void visit(ThisExprNode node) {

    }

    @Override
    public void visit(CallExprNode node) {

    }

    @Override
    public void visit(IfStmtNode node) {

    }

    @Override
    public void visit(BreakStmtNode node) {

    }

    @Override
    public void visit(WhileStmtNode node) {

    }

    @Override
    public void visit(ContinueStmtNode node) {

    }

    @Override
    public void visit(ExprStmtNode node) {

    }

    @Override
    public void visit(ForStmtNode node) {

    }

    @Override
    public void visit(ReturnStmtNode node) {

    }

    @Override
    public void visit(VarDecStmtNode node) {

    }

    @Override
    public void visit(ParameterNode node) {

    }

    @Override
    public void visit(MethodDecNode node) {

    }
}
