package Frontend;

import AST.*;
import Tools.Location;
import Tools.MXError;
import Tools.MXLogger;
import Tools.Operators;
import com.antlr.MxBaseVisitor;
import com.antlr.MxParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    private MXLogger logger;
    public ASTBuilder(MXLogger logger) {
        this.logger = logger;
    }

    @Override
    public ASTNode visitMxProgram(MxParser.MxProgramContext ctx) {
        Location location = new Location(ctx);
        List<DecNode> decNodeList = new ArrayList<DecNode>();
        if (ctx.declaration() != null) {
            for (ParserRuleContext subctx : ctx.declaration()) {
                ASTNode subnode = visit(subctx);
                decNodeList.add((DecNode) subnode);
            }
        }
        return new MxProgramNode(location, decNodeList);
    }

    @Override
    public ASTNode visitDeclaration(MxParser.DeclarationContext ctx) {
        if (ctx.classDeclaration() != null)
            return visit(ctx.classDeclaration());
        if (ctx.funcDeclaration() != null)
            return visit(ctx.funcDeclaration());
        if (ctx.variableDeclaration() != null)
            return visit(ctx.variableDeclaration());
        System.err.println("Declaration node has no sub node");
        return super.visitDeclaration(ctx);
    }

    @Override
    public ASTNode visitMethodDeclaration(MxParser.MethodDeclarationContext ctx) {
        BlockNode block = (BlockNode) visit(ctx.block());
        String id = ctx.IDENTIFIER().getText();
        List<ParameterNode> paras = new ArrayList<ParameterNode>();
        if (ctx.parameters().parameterList() != null) {
            for (MxParser.ParameterContext subctx : ctx.parameters().parameterList().parameter()) {
                paras.add((ParameterNode) visit(subctx));
            }
        }
        if (ctx.typeTypeOrVoid() != null) {
            TypeNode type = (TypeNode) visit(ctx.typeTypeOrVoid());
            return new MethodDecNode(new Location(ctx), block, type, paras, false, id);
        }
        return new MethodDecNode(new Location(ctx), block, null, paras, true, id);
    }

    @Override
    public ASTNode visitParameter(MxParser.ParameterContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        TypeNode type = (TypeNode) visit(ctx.typeType());
        return new ParameterNode(new Location(ctx), type, id);
    }

    @Override
    public ASTNode visitVariableDeclaration(MxParser.VariableDeclarationContext ctx) {
        Location location = new Location(ctx);
        TypeNode vartype = (TypeNode) visit(ctx.typeTypeOrVoid());
        List<VarDecoratorNode> VarDecoList = new ArrayList<VarDecoratorNode>();
        for (MxParser.VariableDecoratorContext subDecoctx : ctx.variableDecorator()) {
            VarDecoList.add((VarDecoratorNode) visit(subDecoctx));
        }
        return new VariableDecNode(location, vartype, VarDecoList);
    }

    @Override
    public ASTNode visitVariableDecorator(MxParser.VariableDecoratorContext ctx) {
        Location location = new Location(ctx);
        String id = ctx.IDENTIFIER().getText();
        if (ctx.expression() != null) {
            ExprNode expr = (ExprNode) visit(ctx.expression());
            return new VarDecoratorNode(location, id, expr);
        }
        return new VarDecoratorNode(location, id, null);
    }


    @Override
    public ASTNode visitClassDeclaration(MxParser.ClassDeclarationContext ctx) {
        /*
         * TODO: Catch exception from Parser, e.g. class int{} will throw an error
         */
        String id = ctx.IDENTIFIER().getText();
        Location location = new Location(ctx);
        List<MethodDecNode> methodDecNodeList = new ArrayList<MethodDecNode>();
        List<VariableDecNode> variableDecNodeList = new ArrayList<VariableDecNode>();
        for (MxParser.ClassBodyDeclarationContext ClassDecctx : ctx.classBody().classBodyDeclaration()) {
            if (ClassDecctx.methodDeclaration() != null) {
                methodDecNodeList.add((MethodDecNode) visit(ClassDecctx.methodDeclaration()));
            } else if (ClassDecctx.variableDeclaration() != null) {
                variableDecNodeList.add((VariableDecNode) visit(ClassDecctx.variableDeclaration()));
            } else {
                System.err.println("Class declaration encounter null node");
            }
        }

        return new ClassDecNode(location, id, variableDecNodeList, methodDecNodeList);
    }

    @Override
    public ASTNode visitFuncDeclaration(MxParser.FuncDeclarationContext ctx) {
        Location location = new Location(ctx);
        String id = ctx.IDENTIFIER().getText();
        TypeNode ReturnType = (TypeNode) visit(ctx.typeTypeOrVoid());
        BlockNode blockNode = (BlockNode) visit(ctx.block());
        List<ParameterNode> paraList = new ArrayList<ParameterNode>();
        if (ctx.parameters().parameterList() != null) {
            for (MxParser.ParameterContext SubPara : ctx.parameters().parameterList().parameter()) {
                paraList.add((ParameterNode) visit(SubPara));
            }
        }
        return new FunctionDecNode(location, blockNode, ReturnType, paraList, id);
    }

    @Override
    public ASTNode visitTypeTypeOrVoid(MxParser.TypeTypeOrVoidContext ctx) {
        if (ctx.VOID() != null) return new TypeNode(new Location(ctx), new Type(BaseType.RTYPE_VOID));
        return visit(ctx.typeType());
    }


    @Override
    public ASTNode visitArrayType(MxParser.ArrayTypeContext ctx) {
        /*
         *
         *  FIXME: DEAL WITH ARRAY TYPE
         *
         */
        TypeNode OriginalType = (TypeNode) visit(ctx.nonArrayTypeNode());
        int arrayLevel = (ctx.getChildCount() - 1) / 2;
        if (arrayLevel == 0 ) {
            return new TypeNode(new Location(ctx), OriginalType.getType());
        } else return new ArrayTypeNode(OriginalType, arrayLevel);
    }

    @Override
    public ASTNode visitNonArrayTypeNode(MxParser.NonArrayTypeNodeContext ctx) {
        if (ctx.classType() != null) {
            return new TypeNode(
                    new Location(ctx),
                    new ClassType(ctx.classType().IDENTIFIER().getText())
            );
        } else {
            MxParser.PrimitiveTypeContext subctx = ctx.primitiveType();
            if (subctx.STRING() != null) {
                return new TypeNode(new Location(ctx), new Type(BaseType.DTYPE_STRING));
            } else if (subctx.INT() != null) {
                return new TypeNode(new Location(ctx), new Type(BaseType.DTYPE_INT));
            } else if (subctx.BOOL() != null) {
                return new TypeNode(new Location(ctx), new Type(BaseType.DTYPE_BOOL));
            }
        }
        return null;
    }

    @Override
    public ASTNode visitNonArrayType(MxParser.NonArrayTypeContext ctx) {
        return visit(ctx.nonArrayTypeNode());
    }

    /*  Stmt function */

    @Override
    public ASTNode visitBlock(MxParser.BlockContext ctx) {
        Location location = new Location(ctx);
        List<StmtNode> stmtList = new ArrayList<StmtNode>();
        for (MxParser.StatementContext substmt : ctx.blockStatement().statement()) {
            stmtList.add((StmtNode) visit(substmt));
        }
        return new BlockNode(location, stmtList);
    }

    @Override
    public ASTNode visitSemiStmt(MxParser.SemiStmtContext ctx) {
        return new SemiStmtNode(new Location(ctx));
    }

    @Override
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        Location location = new Location(ctx);
        ExprNode expr = (ExprNode) visit(ctx.expression());
        List<MxParser.StatementContext> ThenElseStmt = ctx.statement();
        StmtNode elseStmt = null, thenStmt = (StmtNode) visit(ThenElseStmt.get(0));
        boolean hasElse = false;
        if (ThenElseStmt.size() == 2) {
            elseStmt = (StmtNode) visit(ThenElseStmt.get(1));
            hasElse = true;
        }
        // else then must be block
        if (!(thenStmt instanceof BlockNode)) {
            ArrayList<StmtNode> stmtNodeArrayList = new ArrayList<>();
            stmtNodeArrayList.add(thenStmt);
            thenStmt = new BlockNode(thenStmt.GetLocation(), stmtNodeArrayList);
        }
        if (hasElse && !(elseStmt instanceof BlockNode)) {
            ArrayList<StmtNode> stmtNodeArrayList = new ArrayList<>();
            stmtNodeArrayList.add(elseStmt);
            elseStmt = new BlockNode(elseStmt.GetLocation(), stmtNodeArrayList);
        }
        return new IfStmtNode(location, expr, thenStmt, elseStmt, hasElse);
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        Location location = new Location(ctx);
        ExprNode expr = (ExprNode) visit(ctx.expression());
        StmtNode loopstmt = (StmtNode) visit(ctx.statement());
        if (loopstmt == null) {
            logger.severe("Fatal error occur.", new Location(ctx));
            System.exit(1);
        }
        if (!(loopstmt instanceof BlockNode)) {
            ArrayList<StmtNode> stmtNodeArrayList = new ArrayList<>();
            stmtNodeArrayList.add(loopstmt);
            loopstmt = new BlockNode(loopstmt.GetLocation(), stmtNodeArrayList);
        }

        return new WhileStmtNode(location, expr, loopstmt);
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        StmtNode loopstmt = (StmtNode) visit(ctx.statement());
        // ensure that loop stmt is a block node
        if (loopstmt == null) {
            logger.severe("Fatal error occur.", new Location(ctx));
            System.exit(1);
        }
        if (!(loopstmt instanceof BlockNode)) {
            ArrayList<StmtNode> stmtNodeArrayList = new ArrayList<>();
            stmtNodeArrayList.add(loopstmt);
            loopstmt = new BlockNode(loopstmt.GetLocation(), stmtNodeArrayList);
        }

        MxParser.ForControlContext forexprs = ctx.forControl();
        ExprNode initExpr=null,  condExpr=null, updateExpr=null;
        if (forexprs.forinit != null) initExpr = (ExprNode) visit(forexprs.forinit);
        if (forexprs.forcond != null) condExpr = (ExprNode) visit(forexprs.forcond);
        if (forexprs.forUpdate != null) updateExpr = (ExprNode) visit(forexprs.forUpdate);

        return new ForStmtNode(new Location(ctx), initExpr, condExpr, updateExpr, loopstmt);
    }

    @Override
    public ASTNode visitVariableDeclStmt(MxParser.VariableDeclStmtContext ctx) {
        return new VarDecStmtNode(new Location(ctx),
                (VariableDecNode) visit(ctx.variableDeclaration()));
    }

    @Override
    public ASTNode visitBlockStmt(MxParser.BlockStmtContext ctx) {
        // return block node
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        Location location = new Location(ctx);
        if (ctx.expression() != null) {
            ExprNode expr = (ExprNode) visit(ctx.expression());
            return new ReturnStmtNode(location, expr);
        } else {
            logger.fine("Return statement at line " + location.getLine() + " has no return value.");
            return new ReturnStmtNode(location, null);
        }
    }

    @Override
    public ASTNode visitBreakStmt(MxParser.BreakStmtContext ctx) {
        return new BreakStmtNode(new Location(ctx));
    }

    @Override
    public ASTNode visitContinueStmt(MxParser.ContinueStmtContext ctx) {
        return new ContinueStmtNode(new Location(ctx));
    }

    @Override
    public ASTNode visitExprStmt(MxParser.ExprStmtContext ctx) {
        ExprNode expr = (ExprNode) visit(ctx.expression());
        return new ExprStmtNode(new Location(ctx), expr);
    }

    /* Expr visit function */

    @Override
    public ASTNode visitBinaryOpExpr(MxParser.BinaryOpExprContext ctx) {
        String op = ctx.bop.getText();
        Operators.BinaryOp bop;
        Location location = new Location(ctx);
        ExprNode LHS = (ExprNode) visit(ctx.expression(0));
        ExprNode RHS = (ExprNode) visit(ctx.expression(1));
        switch (op) {
            case "*":
                bop = Operators.BinaryOp.MUL;
                break;
            case "/":
                bop = Operators.BinaryOp.DIV;
                break;
            case "%":
                bop = Operators.BinaryOp.MOD;
                break;
            case "+":
                bop = Operators.BinaryOp.ADD;
                break;
            case "-":
                bop = Operators.BinaryOp.SUB;
                break;
            case "<<":
                bop = Operators.BinaryOp.SHL;
                break;
            case ">>":
                bop = Operators.BinaryOp.SHR;
                break;
            case "<=":
                bop = Operators.BinaryOp.LESS_EQUAL;
                break;
            case ">=":
                bop = Operators.BinaryOp.GREATER_EQUAL;
                break;
            case "<":
                bop = Operators.BinaryOp.LESS;
                break;
            case ">":
                bop = Operators.BinaryOp.GREATER;
                break;
            case "==":
                bop = Operators.BinaryOp.EQUAL;
                break;
            case "!=":
                bop = Operators.BinaryOp.NEQUAL;
                break;
            case "&":
                bop = Operators.BinaryOp.BITWISE_AND;
                break;
            case "^":
                bop = Operators.BinaryOp.BITWISE_XOR;
                break;
            case "|":
                bop = Operators.BinaryOp.BITWISE_OR;
                break;
            case "&&":
                bop = Operators.BinaryOp.LOGIC_AND;
                break;
            case "||":
                bop = Operators.BinaryOp.LOGIC_OR;
                break;
            case "=":
                bop = Operators.BinaryOp.ASSIGN;
                break;
            default:
                throw new MXError("Binary op:"+ op + " is not valid", LHS.GetLocation());
                // bop = Operators.BinaryOp.DEFAULT;
        }

        return new BinExprNode(location, bop, LHS, RHS);
    }

    @Override
    public ASTNode visitPrefixExpr(MxParser.PrefixExprContext ctx) {
        String op = ctx.prefix.getText();
        Location location = new Location(ctx);
        Operators.PreFixOp preFixOp;
        ExprNode expr = (ExprNode) visit(ctx.expression());
        switch (op) {
            case "++":
                preFixOp = Operators.PreFixOp.INC;
                break;
            case "--":
                preFixOp = Operators.PreFixOp.DEC;
                break;
            case "+":
                preFixOp = Operators.PreFixOp.POS;
                break;
            case "-":
                preFixOp = Operators.PreFixOp.NEG;
                break;
            case "!":
                preFixOp = Operators.PreFixOp.LOGIC_NOT;
                break;
            case "~":
                preFixOp = Operators.PreFixOp.BITWISE_NOT;
                break;
            default: {
                preFixOp = Operators.PreFixOp.DEFAULT;
                System.err.println("Prefix Operators not exist");
            }
        }
        return new PrefixExprNode(location, preFixOp, expr);
    }

    @Override
    public ASTNode visitPostfixExpr(MxParser.PostfixExprContext ctx) {
        String op = ctx.postfix.getText();
        Location location = new Location(ctx);
        Operators.PostFixOp postFixOp;
        ExprNode expr = (ExprNode) visit(ctx.expression());
        switch (op) {
            case "++":
                postFixOp = Operators.PostFixOp.INC;
                break;
            case "--":
                postFixOp = Operators.PostFixOp.DEC;
                break;
            default:
                postFixOp = Operators.PostFixOp.DEFAULT;
        }
        return new PostfixExprNode(location, postFixOp, expr);
    }

    @Override
    public ASTNode visitArrayExpr(MxParser.ArrayExprContext ctx) {
        ExprNode arrayid = (ExprNode) visit(ctx.expression(0));
        ExprNode offset = (ExprNode) visit(ctx.expression(1));
        return new ArrayExprNode(new Location(ctx), arrayid, offset);
    }

    @Override
    public ASTNode visitMemberExpr(MxParser.MemberExprContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        ExprNode expr = (ExprNode) visit(ctx.expression());
        return new MemberExprNode(new Location(ctx), expr, id);
    }

    @Override
    public ASTNode visitMethodCallExpr(MxParser.MethodCallExprContext ctx) {
        ExprNode obj = (ExprNode) visit(ctx.expression());
        List<ExprNode> paras = new ArrayList<ExprNode>();
        if (ctx.expressionList() != null) {
            for (MxParser.ExpressionContext subctx : ctx.expressionList().expression()) {
                paras.add((ExprNode) visit(subctx));
            }
        }
        return new CallExprNode(new Location(ctx), obj, paras);
    }


    @Override
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        return visit(ctx.creator());
    }

    @Override
    public ASTNode visitArrayCreator(MxParser.ArrayCreatorContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.nonArrayTypeNode());
        List<ExprNode> exprNodeList = new ArrayList<>();

        // Deciding if the shape is correctly specified.
        boolean isEnd = false;
        for (MxParser.ArraySizeContext subctx: ctx.arraySize()) {
            if (subctx.expression() != null ) {
                if (isEnd) {
                    logger.severe("The shape of multidimensional array must be specified from left to right",
                            new Location(ctx));
                }
                exprNodeList.add((ExprNode) visit(subctx.expression()));
            } else {
                isEnd = true;
            }
        }

        int arrayLevel = ctx.arraySize().size();
        return new ArrayCreatorNode(new Location(ctx), type, exprNodeList, arrayLevel);
    }

    @Override
    public ASTNode visitConstructorCreator(MxParser.ConstructorCreatorContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.nonArrayTypeNode());
        if (ctx.getChildCount() > 1) {
            return new ConstructCreatorNode(new Location(ctx), type, true);
        }
        return new ConstructCreatorNode(new Location(ctx), type, false);
    }

    @Override
    public ASTNode visitTerminal(TerminalNode node) {
        System.err.println("Should've handle all the terminals");
        return null;
    }

    @Override
    public ASTNode visitPrimaryExpr(MxParser.PrimaryExprContext ctx) {
        return visit(ctx.primary());
    }


    @Override
    public ASTNode visitNameExpr(MxParser.NameExprContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        return new IDExprNode(new Location(ctx), id);
    }

    @Override
    public ASTNode visitThisExpr(MxParser.ThisExprContext ctx) {
        return new ThisExprNode(new Location(ctx));
    }

    @Override
    public ASTNode visitLiteralExpr(MxParser.LiteralExprContext ctx) {
        MxParser.LiteralContext subctx = ctx.literal();
        if (subctx.BOOL_LITERAL() != null) {
            boolean value = Boolean.valueOf(subctx.BOOL_LITERAL().getText());
            return new BoolConstNode(new Location(ctx), value);
        } else if (subctx.DECIMAL_LITERAL() != null) {
            int value = Integer.valueOf(subctx.DECIMAL_LITERAL().getText());
            return new IntConstNode(new Location(ctx), value);
        } else if (subctx.STRING_LITERAL() != null) {
            String value = subctx.STRING_LITERAL().getText();
            return new StringConstNode(new Location(ctx), value);
        } else if (subctx.NULL_LITERAL() != null) {
            return new NullConstNode(new Location(ctx));
        }
        System.err.println("Visit Literal Error");
        return null;
    }

    @Override
    public ASTNode visitParenthesizedExpr(MxParser.ParenthesizedExprContext ctx) {
        return visit(ctx.expression());
    }
}
