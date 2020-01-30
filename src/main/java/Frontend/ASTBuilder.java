package Frontend;

import AST.*;
import com.antlr.MxBaseVisitor;
import com.antlr.MxParser;
import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitMxProgram(MxParser.MxProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public ASTNode visitDeclaration(MxParser.DeclarationContext ctx) {
        return visitChildren(ctx);
    }

}
