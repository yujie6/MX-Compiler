package Frontend;

import AST.*;
import MxEntity.*;
import Tools.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;


public class SemanticChecker implements ASTVisitor {

    /**
     * @implSpec Declaration, block and loop have their
     * own scope, and the LocalScope will change when visiting
     * these nodes
     * @apiNote 1. Check if a var/func/class is defined
     * 2. Check if they are defined more than once
     */
    final private Type FunctionType;
    final private Type BoolType;
    final private Type IntType;
    final private Type StringType;
    private Logger logger;


    private Scope GlobalScope, LocalScope;
    private Stack<Scope> EnteredScope;

    private FunctionEntity CurrFunctionCall;
    private ClassEntity CurrClass;

    public SemanticChecker(Scope gs, Logger logger) {
        FunctionType = new Type(BaseType.STYPE_FUNC);
        BoolType = new Type(BaseType.DTYPE_BOOL);
        StringType = new Type(BaseType.DTYPE_STRING);
        IntType = new Type(BaseType.DTYPE_INT);
        EnteredScope = new Stack<>();
        this.GlobalScope = gs;
        this.logger = logger;
    }

    boolean isValid(Type type) {
        if (type.isClass()) {
            return GlobalScope.hasClass(type.getName());
        }
        return true;
    }

    private void EnterScope(Entity mx_entity) {
        EnteredScope.push(LocalScope);
        if (mx_entity != null) LocalScope = mx_entity.getScope();
    }

    private void ExitScope() {
        LocalScope = EnteredScope.lastElement();
        EnteredScope.pop();
    }

    private Scope getFatherScope() {
        return EnteredScope.lastElement();
    }

    @Override
    public void visit(MxProgramNode node) {
        LocalScope = GlobalScope;
        if (LocalScope.inClass) {
            throw new MXError("Fatal error");
        }
        for (DecNode declaration : node.getDecNodeList()) {
            if (! (declaration instanceof  VariableDecNode)) {
                declaration.accept(this);
            }
        }
        logger.info("Semantic checks successfully, no error detected.");
    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity functionEntity;
        if (!LocalScope.inClass) {
            functionEntity = LocalScope.GetFunction(node.getIdentifier());
        } else {
            functionEntity = CurrClass.getMethod(node.getIdentifier());
        }
        EnterScope(functionEntity);
        LocalScope.inFunction = true;
        LocalScope.setFuncRetType(node.getReturnType().getType());
        if (!isValid(LocalScope.getFuncRetType())) {
            throw new MXError(String.format("The class or type %s is not defined.",
                    node.getReturnType().getType().getName()), node.GetLocation());
        }

        for (ParameterNode para : node.getParaDecList()) {

            VariableEntity mx_para = new VariableEntity(LocalScope, para);
            if (!isValid(mx_para.getVarType()) ) {
                throw new MXError(String.format("The class or type %s is not defined.",
                        mx_para.getVarType().getName()), node.GetLocation());
            }
            mx_para.setScopeLevel(EnteredScope.size());
            LocalScope.defineVariable(mx_para);
        }
        for (StmtNode statement : node.getFuncBlock().getStmtList()) {
            statement.accept(this);
        }
        ExitScope();
        logger.fine("Semantic checks on function \'" + node.getIdentifier() + "\' successfully.");
        LocalScope.inFunction = false;
    }

    @Override
    public void visit(VariableDecNode node) {
        Type DecType = node.getType();
        // check if the type is defined
        if (!isValid(DecType)) {
            if (!LocalScope.hasClass(DecType.getName())) {
                throw new MXError("Invalid class: " + DecType.getName(), node.GetLocation());
            }
        }
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            VariableEntity mx_var = new VariableEntity(LocalScope, var, DecType);
            if (LocalScope.hasVariable(mx_var.getIdentifier())) {
                // checking variable redefinition
                // check if last scope have this var
                VariableEntity exist_var = LocalScope.GetVariable(mx_var.getIdentifier());
                if (mx_var.getScopeLevel() == exist_var.getScopeLevel()) {
                    throw new MXError(String.format("Variable %s has been defined twice in the same scope"
                            , mx_var.getIdentifier()), node.GetLocation());
                }

            }
            mx_var.setScopeLevel(EnteredScope.size());
            LocalScope.defineVariable(mx_var);
        }
    }

    @Override
    public void visit(ClassDecNode node) {
        ClassEntity classEntity = GlobalScope.GetClass(node.getIdentifier());
        CurrClass = classEntity;
        EnterScope(classEntity);
        LocalScope.inClass = true;
        for (VariableDecNode MemberDecNode : node.getVarNodeList()) {
            visit(MemberDecNode);
        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            visit(method);
        }
        ExitScope();
        LocalScope.inClass = false;
        CurrClass = null;
        logger.fine("Semantic checks on class \'" + node.getIdentifier() + "\' done successfully.");
    }

    @Override
    public void visit(MethodDecNode node) {
        visit((FunctionDecNode) node);
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(BlockNode node) {
        EnterScope(null);
        for (StmtNode statement : node.getStmtList()) {
            statement.accept(this);
        }
        ExitScope();
    }

    @Override
    public void visit(VarDecoratorNode node) {

    }

    @Override
    public void visit(ConstNode node) {
        node.setExprType(node.getType());
    }

    @Override
    public void visit(ArrayCreatorNode node) {
        int arrayLevel = node.getExprList().size() + node.getArrayLevel();
        if (node.getExprType().isClass()) {
            node.setExprType(new Type(BaseType.STYPE_CLASS, arrayLevel,
                    node.getExprType().getName()));
        } else {
            node.setExprType(new Type(node.getExprType().getBaseType(), arrayLevel,
                    node.getExprType().getName()));
        }
    }

    @Override
    public void visit(ConstructCreatorNode node) {

    }

    @Override
    public void visit(BinExprNode node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        if (!node.getLeftExpr().getExprType().equals(node.getRightExpr().getExprType())) {
            throw new MXError("Expr type mismatch.", node.GetLocation());
        }

        switch (node.getBop()) {
            case ASSIGN: {
                // Assign statement: check left value (Array/member/id)
                ExprNode LHS = node.getLeftExpr();
                if (!(LHS instanceof MemberExprNode || LHS instanceof IDExprNode ||
                        LHS instanceof ArrayExprNode)) {
                    throw new MXError("Assign statement has wrong left value.", node.GetLocation());
                }
                node.setExprType(node.getLeftExpr().getExprType());
                break;
            }
            case GREATER:
            case GREATER_EQUAL:
            case LESS_EQUAL:
            case LESS: {
                if (!node.getLeftExpr().getExprType().isInt()
                        && !node.getRightExpr().getExprType().isString()) {
                    throw new MXError("Operator <,>,<=,>= can only be used by integers or string",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }
            case NEQUAL:
            case EQUAL: {
                if (!node.getLeftExpr().getExprType().isBool() &&
                        !node.getLeftExpr().getExprType().isInt() &&
                        !node.getLeftExpr().getExprType().isString()) {
                    throw new MXError("Operators ==,!= does not support this type.",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }

            case LOGIC_OR:
            case LOGIC_AND: {
                if (!node.getLeftExpr().getExprType().isBool()) {
                    throw new MXError("Operators ||,&& only support bool type.", node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }

            case BITWISE_AND:
            case BITWISE_OR:
            case BITWISE_XOR:
            case DIV:
            case MUL:
            case SHL:
            case SHR:
            case MOD:
            case SUB: {
                if (!node.getLeftExpr().getExprType().isInt()) {
                    throw new MXError("Operators used on wrong type, expect int.", node.GetLocation());
                }
                node.setExprType(IntType);
                break;
            }

            case ADD: {
                if (!node.getLeftExpr().getExprType().isInt() &&
                !node.getLeftExpr().getExprType().isString()) {
                    throw new MXError("Only int and string support + operator.", node.GetLocation());
                }
                node.setExprType(node.getLeftExpr().getExprType());
                break;
            }
            default: {
                throw new MXError("Wrong operators.", node.GetLocation());
            }
        }
    }

    @Override
    public void visit(IDExprNode node) {
        // this node can only be NameExpr
        if (node.getExprType() == FunctionType) {
            CurrFunctionCall = GlobalScope.GetFunction(node.getIdentifier());
        } else if (node.getExprType() == null) {
            if (!LocalScope.hasVariable(node.getIdentifier())) {
                throw new MXError("Variable not defined!", node.GetLocation());
            }
            VariableEntity mx_var = LocalScope.GetVariable(node.getIdentifier());
            node.setExprType(mx_var.getVarType());
        }
    }

    @Override
    public void visit(MemberExprNode node) {
        // after type checking
        node.getExpr().accept(this);
        if (node.getExpr().getExprType().isString()) {
            CurrFunctionCall = GlobalScope.GetFunction("string." + node.getMember());
            return;
        } else if (node.getExpr().getExprType().isArray() && node.getMember().equals("size")) {
            ClassEntity ArrayEntitry = GlobalScope.GetClass("Array");
            CurrFunctionCall = ArrayEntitry.getMethod("size");
            return;
        }
        if (!node.getExpr().getExprType().isClass()) {
            throw new MXError("This expr has no member access", node.GetLocation());
        }
        String ClassName = node.getExpr().getExprType().getName();
        ClassEntity classEntity = GlobalScope.GetClass(ClassName);
        if (node.getExprType() == FunctionType) {
            CurrFunctionCall = classEntity.getMethod(node.getMember());
            // node.
        } else {
            VariableEntity member = classEntity.getMember(node.getMember());
            node.setExprType(member.getVarType());
        }
    }

    @Override
    public void visit(ArrayExprNode node) {
        ExprNode arrayId = node.getArrayId();
        arrayId.accept(this);
        Type OriginalType = arrayId.getExprType();
        if (!OriginalType.isArray()) {
            throw new MXError("This ID has no array access!", node.GetLocation());
        }
        node.getOffset().accept(this);
        Type offsetType = node.getOffset().getExprType();
        if (!offsetType.isInt() || offsetType.isArray()) {
            throw new MXError("Offset of array must be int.", node.GetLocation());
        }
        node.setExprType(new Type(OriginalType.getBaseType(),
                OriginalType.getArrayLevel() - 1,
                OriginalType.getName()));
    }

    @Override
    public void visit(PrefixExprNode node) {
        node.getExpr().accept(this);

        switch (node.getPrefixOp()) {
            case DEC:
            case BITWISE_NOT:
            case INC:
            case POS:
            case NEG: {
                if (! node.getExpr().getExprType().isInt()) {
                    throw new MXError("Prefix op ++,--,+,- could only be used for int",
                            node.GetLocation());
                }
                node.setExprType(IntType);
                break;
            }

            case LOGIC_NOT: {
                if (!node.getExpr().getExprType().isBool()) {
                    throw new MXError("Logic not can only be used for bool value.",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }
            case DEFAULT: {
                throw new MXError("Wrong prefix op detected.", node.GetLocation());
            }
        }
    }

    @Override
    public void visit(PostfixExprNode node) {
        node.getExpr().accept(this);
        switch (node.getPostfixOp()) {
            case INC:
            case DEC: {
                if (!node.getExpr().getExprType().isInt()) {
                    throw new MXError("Postfix op ++,-- could only be used for int.",
                            node.GetLocation());
                }
                node.setExprType(IntType);
                break;
            }
            case DEFAULT: {
                throw new MXError("Unknown error for postfix op detected",
                        node.GetLocation());
            }
        }

    }

    @Override
    public void visit(ThisExprNode node) {
        if (!LocalScope.inClass) {
            throw new MXError("Using \"this\" out of a class domain.", node.GetLocation());
        }
    }

    @Override
    public void visit(CallExprNode node) {
        node.getObj().setExprType(FunctionType);
        node.getObj().accept(this);
        node.setFunction(CurrFunctionCall);
        ArrayList<VariableEntity> ExpectedParas = CurrFunctionCall.getParaList();
        if (node.getParameters() == null) {
            if (CurrFunctionCall.getParaListSize() != 0) {
                throw new MXError("Function call needs parameters", node.GetLocation());
            }
            return;
        }
        if (ExpectedParas.size() != node.getParameters().size()) {
            throw new MXError("Function call has wrong number of parameters", node.GetLocation());
        }
        if (ExpectedParas.size() != 0) {
            for (int i = 0; i < ExpectedParas.size(); i++) {
                Type ExpectedType = ExpectedParas.get(i).getVarType();
                ExprNode para_i = node.getParameters().get(i);
                para_i.accept(this);
                // TODO: Another problem is that we cannot compare the type directly
                if (!ExpectedType.equals(para_i.getExprType())) {
                    throw new MXError(String.format("Function call has wrong parameter type for the %d th parameter", i)
                            , node.GetLocation());
                }
            }
        }
        node.setExprType(node.getFunction().getReturnType());
        logger.fine("Semantic checks on \'" + node.getFunction().getIdentifier() + "\' function call at line "
                + node.GetLocation().getLine() + " done successfully.");
        // node.getParameters().forEach();
    }

    @Override
    public void visit(IfStmtNode node) {
        node.getConditionExpr().accept(this);
        if (!node.getConditionExpr().getExprType().isBool()) {
            throw new MXError("If statement's condition is not bool type.", node.GetLocation());
        }
        node.getThenStmt().accept(this);
        if (node.isHasElse()) {
            node.getElseStmt().accept(this);
        }
    }

    @Override
    public void visit(BreakStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            throw new MXError("Continue statement not in a loop");
        }
    }

    @Override
    public void visit(WhileStmtNode node) {
        LocalScope.LoopLevel++;
        if (node.getCondition() == null) {
            throw new MXError("While statement has no condition expr.", node.GetLocation());
        } else {
            node.getCondition().accept(this);
            if (!node.getCondition().getExprType().isBool()) {
                throw new MXError("While statement condition is not bool.", node.GetLocation());
            }
        }
        node.getLoopStmt().accept(this);
        LocalScope.LoopLevel--;
        logger.fine("Semantic checks on \'while statement\' at line "
                + node.GetLocation().getLine() + " done successfully.");
    }

    @Override
    public void visit(ContinueStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            throw new MXError("Continue statement not in a loop");
        }
    }

    @Override
    public void visit(ExprStmtNode node) {
        // damn it ++a; (a); (++a); a + a; are all valid statement!!
        // only <array,member,id> can be count as left value
        // a = 123 for example, what  about a = b = 1 ?
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ForStmtNode node) {
        LocalScope.LoopLevel++;
        if (node.getCondExpr() != null) {
            node.getCondExpr().accept(this);
            if (!node.getCondExpr().getExprType().isBool()) {
                throw new MXError("For condition type error.", node.GetLocation());
            }
        }
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
        node.getLoopBlcok().accept(this);
        LocalScope.LoopLevel--;
        logger.fine("Semantic checks on \'for statement\' at line "
                + node.GetLocation().getLine() + " done successfully.");
    }

    @Override
    public void visit(ReturnStmtNode node) {
        node.getReturnedExpr().accept(this);
        if (LocalScope.inFunction = false) {
            throw new MXError("Return statement not in a function");
        }
        Type RetType = node.getReturnedExpr().getExprType();
        if (!LocalScope.getFuncRetType().equals(RetType)) {
            throw new MXError("Return type mismatch.", node.GetLocation());
        }

    }

    @Override
    public void visit(VarDecStmtNode node) {
        visit(node.getVariableDecNode());
    }

    @Override
    public void visit(ParameterNode node) {

    }
}
