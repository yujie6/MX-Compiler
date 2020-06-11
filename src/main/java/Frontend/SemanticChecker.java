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
    static public MXLogger logger;


    private Scope GlobalScope, LocalScope;
    private Stack<Scope> EnteredScope;

    private FunctionEntity CurrFunctionCall;
    private ClassEntity CurrClass;
    private boolean hasRetStmt, inConstructMethod;

    public SemanticChecker(Scope gs, MXLogger logger) {
        FunctionType = new Type(BaseType.STYPE_FUNC);
        BoolType = new Type(BaseType.DTYPE_BOOL);
        StringType = new Type(BaseType.DTYPE_STRING);
        IntType = new Type(BaseType.DTYPE_INT);
        EnteredScope = new Stack<>();
        this.GlobalScope = gs;
        SemanticChecker.logger = logger;
    }

    public boolean isValid(Type type) {
        if (type.isClass()) {
            return GlobalScope.hasClass(type.getName());
        }
        return true;
    }

    private void EnterScope(Entity mx_entity) {
        EnteredScope.push(LocalScope.clone());
        if (mx_entity != null) LocalScope = mx_entity.getScope();
    }

    private void ExitScope() {
        LocalScope = EnteredScope.peek();
        EnteredScope.pop();
    }

    private boolean setInitForGVar;

    @Override
    public Object visit(MxProgramNode node) {
        LocalScope = GlobalScope;
        if (LocalScope.inClass) {
            logger.severe("Fatal error", node.GetLocation());
            System.exit(1);
        }
        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof ClassDecNode)
                declaration.accept(this);
        }
        setInitForGVar = false;
        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof FunctionDecNode)
                declaration.accept(this);
        }
        setInitForGVar = true;
        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof VariableDecNode)
                declaration.accept(this);
        }


        if (logger.getErrorNum() > 0) {
            System.err.println("Compiler ended with " + logger.getErrorNum() + " semantic errors.");
            System.exit(1);
        } else logger.info("Semantic checks successfully, no error detected.");
        return null;
    }

    @Override
    public Object visit(FunctionDecNode node) {
        FunctionEntity functionEntity;
        if (!LocalScope.inClass) {
            functionEntity = LocalScope.GetFunction(node.getIdentifier());
        } else {
            functionEntity = CurrClass.getMethod(node.getIdentifier());
        }
        EnterScope(functionEntity);
        LocalScope.inFunction = true;
        if (node instanceof MethodDecNode) {
            inConstructMethod = ((MethodDecNode) node).isConstructMethod();
        } else {
            inConstructMethod = false;
        }


        LocalScope.setFuncRetType(functionEntity.getReturnType());
        if (!isValid(LocalScope.getFuncRetType())) {
            logger.severe(String.format("The class or type %s is not defined.",
                    node.getReturnType().getType().getName()), node.GetLocation());
        }
        hasRetStmt = false;
        for (StmtNode statement : node.getFuncBlock().getStmtList()) {
            statement.accept(this);
        }
        if (functionEntity.getReturnType().getBaseType() != BaseType.RTYPE_VOID) {
            if (!hasRetStmt && !functionEntity.isMethod()) {
                logger.warning("Return statement for function '" + node.getIdentifier() +
                        "' does not exist.", node.GetLocation());
            }
        }
        ExitScope();
        logger.fine("Semantic checks on function '" + node.getIdentifier() + "' successfully.");
        LocalScope.inFunction = false;
        return null;
    }

    @Override
    public Object visit(VariableDecNode node) {
        Type DecType = node.getType();
        // check if the type is defined
        if (!isValid(DecType)) {
            if (!LocalScope.hasClass(DecType.getName())) {
                logger.severe("Invalid class: " + DecType.getName(), node.GetLocation());
            }
        }
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            if (setInitForGVar) {
                visit(var);
                return null;
            }
            if (GlobalScope.hasClass(var.getIdentifier())) {
                logger.severe("Variable name and class name for '" +
                        var.getIdentifier() + "' is duplicated", node.GetLocation());
            }

            VariableEntity mx_var = new VariableEntity(LocalScope, var, DecType);
            if (mx_var.getIdentifier().startsWith("_")) {
                logger.severe("_ cannot be the first symbol of identifier", node.GetLocation());
            }

            visit(var);
            if (var.getInitValue() != null) {
                if (!DecType.equals(var.getInitType())) {
                    logger.severe("Value initialization for '" + var.getIdentifier() +
                            "' type mismatch.", node.GetLocation());
                }
            }
            mx_var.setScopeLevel(EnteredScope.size());
            if (LocalScope.hasVariable(mx_var.getIdentifier())) {
                // checking variable redefinition
                // check if last scope have this var
                VariableEntity exist_var = LocalScope.GetVariable(mx_var.getIdentifier());
                if (mx_var.getScopeLevel() == exist_var.getScopeLevel()) {
                    logger.severe(String.format("Variable %s has been defined twice in the same scope"
                            , mx_var.getIdentifier()), node.GetLocation());
                }

            }
            LocalScope.defineVariable(mx_var);
        }
        return null;
    }

    @Override
    public Object visit(ClassDecNode node) {
        ClassEntity classEntity = GlobalScope.GetClass(node.getIdentifier());
        CurrClass = classEntity;
        EnterScope(classEntity);
        LocalScope.inClass = true;
//        for (VariableDecNode MemberDecNode : node.getVarNodeList()) {
//            visit(MemberDecNode);
//        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            visit(method);
        }
        ExitScope();
        LocalScope.inClass = false;
        CurrClass = null;
        logger.fine("Semantic checks on class '" + node.getIdentifier() + "' done successfully.");
        return null;
    }

    @Override
    public Object visit(MethodDecNode node) {
        visit((FunctionDecNode) node);
        return null;
    }

    @Override
    public Object visit(TypeNode node) {
        return null;
    }

    @Override
    public Object visit(BlockNode node) {
        EnterScope(null);
        for (StmtNode statement : node.getStmtList()) {
            statement.accept(this);
        }
        ExitScope();
        return null;
    }

    @Override
    public Object visit(VarDecoratorNode node) {
        if (node.getInitValue() != null) {
            node.getInitValue().accept(this);
            node.setInitType(node.getInitValue().getExprType());
        }
        return null;
    }

    @Override
    public Object visit(ConstNode node) {
        node.setExprType(node.getType());
        return null;
    }

    @Override
    public Object visit(ArrayCreatorNode node) {
        int arrayLevel = node.getArrayLevel();
        for (ExprNode expr : node.getExprList()) {
            expr.accept(this);
            if (!expr.getExprType().isInt()) {
                logger.severe("The size of array can only be int type.", node.GetLocation());
            }
        }
        if (node.getExprType().isClass()) {
            node.setExprType(new Type(BaseType.STYPE_CLASS, arrayLevel,
                    node.getExprType().getName()));
        } else {
            node.setExprType(new Type(node.getExprType().getBaseType(), arrayLevel,
                    node.getExprType().getName()));
        }
        logger.fine("Semantic checks on array creator for " + node.getExprType().getName() + " at line "
                + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(ConstructCreatorNode node) {
        Type newType = node.getExprType();
        node.setExprType(newType);
        logger.info("Use new statement to create object at line " + node.GetLocation().getLine());
        return null;
    }

    @Override
    public Object visit(BinExprNode node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);

        if (node.getLeftExpr() instanceof ThisExprNode) {
            logger.severe("'this' can not be left value.", node.GetLocation());
        }
        if (!node.getLeftExpr().getExprType().equals(node.getRightExpr().getExprType())) {
            logger.severe("Expr type mismatch.", node.GetLocation());
        }

        switch (node.getBop()) {
            case ASSIGN: {
                // Assign statement: check left value (Array/member/id)
                ExprNode LHS = node.getLeftExpr();
                // TODO could be simplified by LHS.isLeftValue
                if (!(LHS instanceof MemberExprNode || LHS instanceof IDExprNode ||
                        LHS instanceof ArrayExprNode)) {
                    logger.severe("Assign statement has wrong left value.", node.GetLocation());
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
                    logger.severe("Operator <,>,<=,>= can only be used by integers or string",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }
            case NEQUAL:
            case EQUAL: {
                if (!node.getLeftExpr().getExprType().isBool() &&
                        !node.getLeftExpr().getExprType().isInt() &&
                        !node.getLeftExpr().getExprType().isString() &&
                        !node.getLeftExpr().getExprType().isNull() &&
                        !node.getRightExpr().getExprType().isNull()) {
                    logger.severe("Operators ==,!= does not support this type.",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }

            case LOGIC_OR:
            case LOGIC_AND: {
                if (!node.getLeftExpr().getExprType().isBool()) {
                    logger.severe("Operators ||,&& only support bool type.", node.GetLocation());
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
                    logger.severe("Operators used on wrong type, expect int.", node.GetLocation());
                }
                node.setExprType(IntType);
                break;
            }

            case ADD: {
                if (!node.getLeftExpr().getExprType().isInt() &&
                        !node.getLeftExpr().getExprType().isString()) {
                    logger.severe("Only int and string support + operator.", node.GetLocation());
                }
                node.setExprType(node.getLeftExpr().getExprType());
                break;
            }
            default: {
                logger.severe("Wrong operators.", node.GetLocation());
            }
        }
        return null;
    }

    @Override
    public Object visit(IDExprNode node) {
        // this node can only be NameExpr
        if (node.getExprType() == FunctionType) {
            if (LocalScope.inClass && CurrClass.hasMethod(node.getIdentifier())) {
                CurrFunctionCall = CurrClass.getMethod(node.getIdentifier());
            } else {
                if (GlobalScope.hasFunction(node.getIdentifier())) {
                    CurrFunctionCall = GlobalScope.GetFunction(node.getIdentifier());
                } else {
                    SemanticChecker.logger.severe("The function " + node.getIdentifier() + " is not defined."
                            , node.GetLocation());
                }
            }
        } else if (node.getExprType() == null) {
            VariableEntity mx_var;
            /*
             * Searching for variable:
             * 1. Local Scope
             * 2. Class member
             * 3. Global Scope
             */
            if (LocalScope.hasVariable(node.getIdentifier())) {
                mx_var = LocalScope.GetVariable(node.getIdentifier());
            } else if (LocalScope.inClass && CurrClass.hasMember(node.getIdentifier())) {
                mx_var = CurrClass.getMember(node.getIdentifier());
            } else if (GlobalScope.hasVariable(node.getIdentifier())) {
                mx_var = GlobalScope.GetVariable(node.getIdentifier());
                if (node.GetLocation().getLine() < mx_var.getLocation().getLine()) {
                    logger.severe("Global Variable " + mx_var.getIdentifier() + " use before defined.",
                            node.GetLocation());
                }
            } else {
                mx_var = null;
            }
            if (mx_var == null) {
                logger.severe("Variable '" + node.getIdentifier() +
                        "' not defined!", node.GetLocation());
                System.err.println("Compiler encountered fatal error.");
                System.exit(1);
            }
            node.setExprType(mx_var.getVarType());
            node.setLeftValue(true);
        }
        return null;
    }

    @Override
    public Object visit(MemberExprNode node) {
        // after type checking
        ClassEntity classEntity;
        node.getExpr().accept(this);
        if (node.getExpr() instanceof ThisExprNode) {
            classEntity = CurrClass;
        } else {
            if (node.getExpr().getExprType().isString()) {
                CurrFunctionCall = GlobalScope.GetFunction("string." + node.getMember());
                return null;
            } else if (node.getExpr().getExprType().isArray() && node.getMember().equals("size")) {
                ClassEntity ArrayEntitry = GlobalScope.GetClass("__Array");
                CurrFunctionCall = ArrayEntitry.getMethod("size");
                return null;
            }
            if (!node.getExpr().getExprType().isClass()) {
                logger.severe("This expr has no member access for '" +
                        node.getMember() + "'.", node.GetLocation());
            }
            String ClassName = node.getExpr().getExprType().getName();
            classEntity = GlobalScope.GetClass(ClassName);
        }

        if (node.getExprType() == FunctionType) {
            if (classEntity.hasMethod(node.getMember())) {
                CurrFunctionCall = classEntity.getMethod(node.getMember());
            } else {
                logger.severe("The method " + node.getMember() + " is not defined.", node.GetLocation());
                System.err.println("Fatal error occur.");
                System.exit(1);
            }
        } else {
            VariableEntity member = classEntity.getMember(node.getMember());
            node.setExprType(member.getVarType());
            node.setLeftValue(true);
        }
        return null;
    }

    @Override
    public Object visit(ArrayExprNode node) {
        ExprNode arrayId = node.getArrayId();
        arrayId.accept(this);
        Type OriginalType = arrayId.getExprType();
        if (!OriginalType.isArray()) {
            logger.severe("This ID has no array access!", node.GetLocation());
        }
        node.getOffset().accept(this);
        Type offsetType = node.getOffset().getExprType();
        if (!offsetType.isInt() || offsetType.isArray()) {
            logger.severe("Offset of array must be int.", node.GetLocation());
        }
        node.setExprType(new Type(OriginalType.getBaseType(),
                OriginalType.getArrayLevel() - 1,
                OriginalType.getName()));
        node.setLeftValue(true);
        return null;
    }

    @Override
    public Object visit(PrefixExprNode node) {
        node.getExpr().accept(this);

        switch (node.getPrefixOp()) {
            case DEC:
            case INC: {
                if (!node.getExpr().getExprType().isInt()) {
                    logger.severe("Prefix operation ++,-- could only be used for int",
                            node.GetLocation());
                }
                node.setExprType(IntType);
                node.setLeftValue(true);
            }
            case BITWISE_NOT:
            case POS:
            case NEG: {
                if (!node.getExpr().getExprType().isInt()) {
                    logger.severe("Prefix operation +,-,~ could only be used for int",
                            node.GetLocation());
                }
                node.setExprType(IntType);
                break;
            }

            case LOGIC_NOT: {
                if (!node.getExpr().getExprType().isBool()) {
                    logger.severe("Logic not can only be used for bool value.",
                            node.GetLocation());
                }
                node.setExprType(BoolType);
                break;
            }
            case DEFAULT: {
                logger.severe("Wrong prefix op detected.", node.GetLocation());
            }
        }
        return null;
    }

    @Override
    public Object visit(PostfixExprNode node) {
        node.getExpr().accept(this);
        switch (node.getPostfixOp()) {
            case INC:
            case DEC: {
                if (!node.getExpr().isLeftValue()) {
                    logger.severe("Only lvalue could use postfix ++,--.", node.GetLocation());
                }
                if (!node.getExpr().getExprType().isInt()) {
                    logger.severe("Postfix op ++,-- could only be used for int.",
                            node.GetLocation());
                }
                node.setExprType(IntType);
                node.setLeftValue(false);
                break;
            }
            case DEFAULT: {
                logger.severe("Unknown error for postfix op detected",
                        node.GetLocation());
            }
        }
        return null;
    }

    @Override
    public Object visit(ThisExprNode node) {
        if (!LocalScope.inClass) {
            logger.severe("Using \"this\" out of a class domain.", node.GetLocation());
        }
        node.setExprType(new ClassType(CurrClass.getIdentifier()));
        return null;
    }

    @Override
    public Object visit(CallExprNode node) {
        node.getObj().setExprType(FunctionType);
        node.getObj().accept(this);
        node.setFunction(CurrFunctionCall);
        ArrayList<VariableEntity> ExpectedParas = CurrFunctionCall.getParaList();
        if (node.getParameters() == null) {
            if (CurrFunctionCall.getParaListSize() != 0) {
                logger.severe("Function call needs parameters", node.GetLocation());
            }
            return null;
        }
        if (ExpectedParas.size() != node.getParameters().size()) {
            logger.severe("Function call has wrong number of parameters", node.GetLocation());
        }
        if (ExpectedParas.size() != 0) {
            for (int i = 0; i < ExpectedParas.size(); i++) {
                Type ExpectedType = ExpectedParas.get(i).getVarType();
                ExprNode para_i = node.getParameters().get(i);
                para_i.accept(this);
                // TODO: Another problem is that we cannot compare the type directly
                if (!ExpectedType.equals(para_i.getExprType())) {
                    logger.severe(String.format("Function call has wrong parameter type for the %d th parameter", i)
                            , node.GetLocation());
                }
            }
        }
        node.setExprType(node.getFunction().getReturnType());
        // logger.fine("Semantic checks on '" + node.getFunction().getIdentifier() + "' function call at line "
        //        + node.GetLocation().getLine() + " done successfully.");
        // node.getParameters().forEach();
        return null;
    }

    @Override
    public Object visit(IfStmtNode node) {
        node.getConditionExpr().accept(this);
        if (!node.getConditionExpr().getExprType().isBool()) {
            logger.severe("If statement's condition is not bool type.", node.GetLocation());
        }
        node.getThenStmt().accept(this);
        if (node.isHasElse()) {
            node.getElseStmt().accept(this);
        }
        // logger.fine("Semantic checks if statement at line "
        //        + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(BreakStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            logger.severe("Continue statement not in a loop", node.GetLocation());
        }
        // logger.fine("Semantic checks break statement at line "
        //       + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(WhileStmtNode node) {
        LocalScope.LoopLevel++;
        if (node.getCondition() == null) {
            logger.severe("While statement has no condition expr.", node.GetLocation());
        } else {
            node.getCondition().accept(this);
            if (!node.getCondition().getExprType().isBool()) {
                logger.severe("While statement condition is not bool.", node.GetLocation());
            }
        }
        node.getLoopStmt().accept(this);
        LocalScope.LoopLevel--;
        // logger.fine("Semantic checks on 'while statement' at line "
        //        + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(ContinueStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            logger.severe("Continue statement not in a loop", node.GetLocation());
        }
        // logger.fine("Semantic checks continue statement at line "
        //        + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(ExprStmtNode node) {
        // damn it ++a; (a); (++a); a + a; are all valid statement!!
        // only <array,member,id> can be count as left value
        // a = 123 for example, what  about a = b = 1 ?
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(ForStmtNode node) {
        LocalScope.LoopLevel++;
        if (node.getCondExpr() != null) {
            node.getCondExpr().accept(this);
            if (!node.getCondExpr().getExprType().isBool()) {
                logger.severe("For condition type error.", node.GetLocation());
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
        // logger.fine("Semantic checks on 'for statement' at line "
        // + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        if (node.getReturnedExpr() != null) node.getReturnedExpr().accept(this);
        if (LocalScope.inFunction = false) {
            logger.severe("Return statement not in a function", node.GetLocation());
        }
        Type RetType = node.getRetType();
        if (inConstructMethod) {
            if (node.getReturnedExpr() != null) {
                logger.severe("Cannot return any value in a constructor.", node.GetLocation());
            }
        } else if (!LocalScope.getFuncRetType().equals(RetType)) {
            logger.severe("Return type mismatch.", node.GetLocation());
        }

        hasRetStmt = true;
        // logger.fine("Semantic checks return statement at line "
        //        + node.GetLocation().getLine() + " done successfully.");
        return null;
    }

    @Override
    public Object visit(VarDecStmtNode node) {
        visit(node.getVariableDecNode());
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
