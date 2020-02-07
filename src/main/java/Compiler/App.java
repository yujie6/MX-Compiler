/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Compiler;

import AST.ASTNode;
import AST.MxProgramNode;
import Frontend.*;
import Tools.MXError;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.antlr.MxLexer;
import com.antlr.MxParser;

import java.io.IOException;
import java.io.InputStream;


public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    private static MxProgramNode GetAbstractSyntaxTree(CharStream input) {
        MxLexer lexer = new MxLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MxParser parser = new MxParser(tokens);
        ParseTree tree = parser.mxProgram();

        ASTBuilder astBuilder = new ASTBuilder();
        MxProgramNode ast = (MxProgramNode) astBuilder.visit(tree);
        System.out.println(tree.toStringTree(parser));
        System.out.println("AST build successfully.");
        return ast;
    }

    private static Scope GetGlobalScope(MxProgramNode ast) {
        GlobalScopeBuilder gsbuilder = new GlobalScopeBuilder();
        gsbuilder.visit(ast);
        System.out.println("GlobalScope build smoothly");
        return gsbuilder.getGlobalScope();
    }

    public static void main(String[] args) throws MXError {
        System.out.println("Application start on " + args[0]);
        CharStream input = CharStreams.fromString("(3 + 65) / 3 - 56");;
        if (args.length == 1) {
            String fileName = String.valueOf(args[0]);
            try {
                input = CharStreams.fromFileName(fileName);
            } catch (IOException e) {
                System.err.println("Read File Failed.\n");
            }
        } else {
            // InputStream is = System.in;
            // input = CharStreams.fromStream(is);
            System.out.println("Use default test text.");
            input = CharStreams.fromString("(3 + 65) / 3 - 56");
        }

        MxProgramNode ast = GetAbstractSyntaxTree(input);
        Scope globalScope = GetGlobalScope(ast);
        (new SemanticChecker(globalScope)).visit(ast);

    }
}
