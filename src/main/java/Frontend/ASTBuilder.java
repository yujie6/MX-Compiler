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
        for (MxParser.ClassBodyDeclarationContext ClassDecctx: ctx.classBody().classBodyDeclaration()) {
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

    /* TODO Expr visit function */

    @Override
    public ASTNode visitBinaryOpExpr(MxParser.BinaryOpExprContext ctx) {
        String op = ctx.bop.getText();
        Operators.BinaryOp bop;
        Location location = new Location(ctx);
        ExprNode LHS = (ExprNode) visit(ctx.expression(0));
        ExprNode RHS = (ExprNode) visit(ctx.expression(1));
        switch (op) {
            case "*": bop = Operators.BinaryOp.MUL; break;
            case "/": bop = Operators.BinaryOp.DIV; break;
            case "%": bop = Operators.BinaryOp.MOD; break;
            case "+": bop = Operators.BinaryOp.ADD; break;
            case "-": bop = Operators.BinaryOp.SUB; break;
            case "<<": bop = Operators.BinaryOp.SHL; break;
            case ">>": bop = Operators.BinaryOp.SHR; break;
            case "<=": bop = Operators.BinaryOp.LESS_EQUAL; break;
            case ">=": bop = Operators.BinaryOp.GREATER_EQUAL; break;
            case "<": bop = Operators.BinaryOp.LESS; break;
            case ">": bop = Operators.BinaryOp.GREATER; break;
            case "==": bop = Operators.BinaryOp.EQUAL; break;
            case "!=": bop = Operators.BinaryOp.NEQUAL; break;
            case "&": bop = Operators.BinaryOp.BITWISE_AND; break;
            case "^": bop = Operators.BinaryOp.BITWISE_XOR; break;
            case "|": bop = Operators.BinaryOp.BITWISE_OR; break;
            case "&&": bop = Operators.BinaryOp.LOGIC_AND; break;
            case "||": bop = Operators.BinaryOp.LOGIC_OR; break;
            default: bop = Operators.BinaryOp.DEFAULT;
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
            case "++": preFixOp = Operators.PreFixOp.INC; break;
            case "--": preFixOp = Operators.PreFixOp.DEC; break;
            case "+": preFixOp = Operators.PreFixOp.POS; break;
            case "-": preFixOp = Operators.PreFixOp.NEG; break;
            case "!": preFixOp = Operators.PreFixOp.LOGIC_NOT; break;
            case "~": preFixOp = Operators.PreFixOp.BITWISE_NOT; break;
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
            case "++": postFixOp = Operators.PostFixOp.INC; break;
            case "--": postFixOp = Operators.PostFixOp.DEC; break;
            default: postFixOp = Operators.PostFixOp.DEFAULT;
        }
        return new PostfixExprNode(location, postFixOp, expr);
    }

    @Override
    public ASTNode visitPrimaryExpr(MxParser.PrimaryExprContext ctx) {
        return super.visitPrimaryExpr(ctx);
    }

    @Override
    public ASTNode visitArrayExpr(MxParser.ArrayExprContext ctx) {
        return super.visitArrayExpr(ctx);
    }
}
