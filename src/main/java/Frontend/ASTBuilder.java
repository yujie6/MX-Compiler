package Frontend;

import AST.*;
import Tools.Location;
import Tools.Operators;
import com.antlr.MxBaseVisitor;
import com.antlr.MxParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
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
        Location location = new Location(ctx);
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
        return super.visitMethodDeclaration(ctx);
    }

    @Override
    public ASTNode visitVariableDeclaration(MxParser.VariableDeclarationContext ctx) {
        Location location = new Location(ctx);
        TypeNode vartype = (TypeNode) visit(ctx.typeType());
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
        ExprNode expr = (ExprNode) visit(ctx.expression());
        return new VarDecoratorNode(location, id, expr);
    }


    @Override
    public ASTNode visitClassDeclaration(MxParser.ClassDeclarationContext ctx) {
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
        List<VariableDecNode> paraList = new ArrayList<VariableDecNode>();
        for (MxParser.ParameterContext SubPara : ctx.parameters().parameterList().parameter()) {
            paraList.add((VariableDecNode) visit(SubPara));
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
        TypeNode OriginalType = (TypeNode) visit(ctx.typeType());
        if (OriginalType instanceof ArrayTypeNode) {
            ArrayTypeNode ori = ((ArrayTypeNode) OriginalType);
            return new ArrayTypeNode(ori.getOriginalType(), ori.getArrayLevel() + 1);
        } else {
            return new ArrayTypeNode(OriginalType, 1);
        }
    }

    @Override
    public ASTNode visitNonArrayType(MxParser.NonArrayTypeContext ctx) {
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

    /* TODO Stmt function */

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
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        Location location = new Location(ctx);
        ExprNode expr = (ExprNode) visit(ctx.expression());
        List<MxParser.StatementContext> ThenElseStmt = ctx.statement();
        StmtNode elsestmt, thenstmt = (StmtNode) visit(ThenElseStmt.get(0));
        if (ThenElseStmt.size() == 2) {
            elsestmt = (StmtNode) visit(ThenElseStmt.get(1));
            return new IfStmtNode(location, expr, thenstmt, elsestmt, true);
        } else {
            return new IfStmtNode(location, expr, thenstmt, null, false);
        }
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        Location location = new Location(ctx);
        ExprNode expr = (ExprNode) visit(ctx.expression());
        StmtNode loopstmt = (StmtNode) visit(ctx.statement());
        return new WhileStmtNode(location, expr, loopstmt);
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        StmtNode loopstmt = (StmtNode) visit(ctx.statement());
        MxParser.ForControlContext forexprs = ctx.forControl();
        ExprNode initExpr = (ExprNode) visit(forexprs.forinit);
        ExprNode condExpr = (ExprNode) visit(forexprs.forcond);
        ExprNode updateExpr = (ExprNode) visit(forexprs.forUpdate);
        return new ForStmtNode(new Location(ctx), initExpr, condExpr, updateExpr, loopstmt);
    }

    @Override
    public ASTNode visitVariableDeclStmt(MxParser.VariableDeclStmtContext ctx) {
        return visit(ctx.variableDeclaration());
    }

    @Override
    public ASTNode visitBlockStmt(MxParser.BlockStmtContext ctx) {
        // return block node
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        Location location = new Location(ctx);
        ExprNode expr = (ExprNode) visit(ctx.expression());
        return new ReturnStmtNode(location, expr);
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

    /* TODO Expr visit function */

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
            default:
                bop = Operators.BinaryOp.DEFAULT;
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
        for (MxParser.ExpressionContext subctx : ctx.expressionList().expression()) {
            paras.add((ExprNode) visit(subctx));
        }
        return new CallExprNode(new Location(ctx), obj, paras);
    }


    @Override
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        return visit(ctx.creator());
    }

    @Override
    public ASTNode visitArrayCreator(MxParser.ArrayCreatorContext ctx) {
        return super.visitArrayCreator(ctx);
    }

    @Override
    public ASTNode visitConstructorCreator(MxParser.ConstructorCreatorContext ctx) {
        return super.visitConstructorCreator(ctx);
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
