package Frontend;

import AST.*;
import MxEntity.*;
import Tools.MXError;
import Tools.MXLogger;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GlobalScopeBuilder implements ASTVisitor {
    private Scope globalScope;
    final private Type FunctionType;
    final private Type BoolType;
    final private Type IntType;
    final private Type StringType;
    final private Type VoidType;
    public static MXLogger logger;

    public GlobalScopeBuilder(MXLogger logger) {
        FunctionType = new Type(BaseType.STYPE_FUNC);
        BoolType = new Type(BaseType.DTYPE_BOOL);
        StringType = new Type(BaseType.DTYPE_STRING);
        IntType = new Type(BaseType.DTYPE_INT);
        VoidType = new Type(BaseType.RTYPE_VOID);
        globalScope = new Scope();
        GlobalScopeBuilder.logger = logger;
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
        // printlnInt(int n) -> end with nothing
        ArrayList<VariableEntity> printInt_para = new ArrayList<>();
        printInt_para.add(new VariableEntity("n", IntType));
        FunctionEntity mx_printInt = new FunctionEntity("printInt", VoidType, null, false,
                globalScope, printInt_para);
        globalScope.defineFunction(mx_printInt);
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
        FunctionEntity mx_size = new FunctionEntity("size", IntType, "__Array", true,
                globalScope, size_para);
        globalScope.defineFunction(mx_size);
        // Array class
        ClassEntity mx_Array = new ClassEntity("__Array", globalScope);
        globalScope.defineClass(mx_Array);
    }

    @Override
    public Object visit(MxProgramNode node) {
        PreProcess();
        for (DecNode declaration : node.getDecNodeList()) {
            if (!(declaration instanceof VariableDecNode))
                declaration.accept(this);
        }

        for (DecNode decNode : node.getDecNodeList() ) {
            if (decNode instanceof VariableDecNode) {
                decNode.accept(this);
            }
        }
        CheckMainEntry();
        return null;
    }

    @Override
    public Object visit(FunctionDecNode node) {
        FunctionEntity mx_function = new FunctionEntity(globalScope, node, false, null);
        if (!mx_function.isMethod() && globalScope.hasClass(mx_function.getIdentifier())) {
            logger.severe("Duplicate name '" + mx_function.getIdentifier() + "' as a function.",
                    node.GetLocation());
        }
        globalScope.defineFunction(mx_function);
        return null;
    }

    @Override
    public Object visit(VariableDecNode node) {
        Type DecType = node.getType();
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            VariableEntity mx_var = new VariableEntity(globalScope, var, DecType);
            if (mx_var.getIdentifier().startsWith("_")) {
                logger.severe("_ cannot be the first symbol of identifier.", node.GetLocation());
            }
            if (globalScope.hasVariable(mx_var.getIdentifier())) {
                logger.severe("The global variable '" + mx_var.getIdentifier() + "' has been defined" +
                        "twice.", node.GetLocation());
            }
            globalScope.defineVariable(mx_var);
        }
        return null;
    }

    @Override
    public Object visit(ClassDecNode node) {
        ClassEntity mx_class = new ClassEntity(globalScope, node);
        if (mx_class.getIdentifier().equals("main")) {
            logger.severe("Duplicated name for main.", node.GetLocation());
        }
        if (globalScope.hasFunction(mx_class.getIdentifier())) {
            logger.severe("Duplicate name '" + mx_class.getIdentifier() + "' as a class.",
                    node.GetLocation());
        }
        globalScope.defineClass(mx_class);
        return null;
    }

    @Override
    public Object visit(MethodDecNode node) {
        return null;
    }

    @Override
    public Object visit(TypeNode node) {
        return null;
    }

    @Override
    public Object visit(VarDecoratorNode node) {
        return null;
    }

    @Override
    public Object visit(IfStmtNode node) {
        return null;
    }

    @Override
    public Object visit(BreakStmtNode node) {
        return null;
    }

    @Override
    public Object visit(WhileStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ContinueStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ExprStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ForStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        return null;
    }

    @Override
    public Object visit(VarDecStmtNode node) {
        return null;
    }

    @Override
    public Object visit(BlockNode node) {
        return null;
    }

    @Override
    public Object visit(ConstNode node) {
        return null;
    }

    @Override
    public Object visit(ArrayCreatorNode node) {
        return null;
    }

    @Override
    public Object visit(ConstructCreatorNode node) {
        return null;
    }

    @Override
    public Object visit(BinExprNode node) {
        return null;
    }

    @Override
    public Object visit(IDExprNode node) {
        return null;
    }

    @Override
    public Object visit(MemberExprNode node) {
        return null;
    }

    @Override
    public Object visit(ArrayExprNode node) {
        return null;
    }

    @Override
    public Object visit(PrefixExprNode node) {
        return null;
    }

    @Override
    public Object visit(PostfixExprNode node) {
        return null;
    }

    @Override
    public Object visit(ThisExprNode node) {
        return null;
    }

    @Override
    public Object visit(CallExprNode node) {
        return null;
    }

    @Override
    public Object visit(ParameterNode node) {
        return null;
    }

    @Override
    public Object visit(SemiStmtNode node) {
        return null;
    }


}
