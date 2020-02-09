/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Compiler;

import AST.ASTNode;
import AST.MxProgramNode;
import Frontend.*;
import Tools.LogFormatter;
import Tools.MXError;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.antlr.MxLexer;
import com.antlr.MxParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    private static MxProgramNode GetAbstractSyntaxTree(CharStream input, Logger logger) {
        MxLexer lexer = new MxLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MxParser parser = new MxParser(tokens);
        ParseTree tree = parser.mxProgram();

        ASTBuilder astBuilder = new ASTBuilder(logger);
        MxProgramNode ast = (MxProgramNode) astBuilder.visit(tree);
        System.out.println(tree.toStringTree(parser));
        logger.info("AST build successfully.");
        return ast;
    }

    private static Scope GetGlobalScope(MxProgramNode ast, Logger logger) {
        GlobalScopeBuilder gsbuilder = new GlobalScopeBuilder(logger);
        gsbuilder.visit(ast);
        logger.info("GlobalScope build smoothly");
        return gsbuilder.getGlobalScope();
    }

    public static void main(String[] args) throws MXError {
        // int[][] a = new int[2][];
        // Setting logger
        Logger logger = Logger.getLogger("MXLogger");
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LogFormatter());
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);
        logger.info("Application start on " + args[0]);


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
            logger.info("Use default test text.");
            input = CharStreams.fromString("(3 + 65) / 3 - 56");
        }

        MxProgramNode ast = GetAbstractSyntaxTree(input, logger);
        Scope globalScope = GetGlobalScope(ast, logger);
        (new SemanticChecker(globalScope, logger)).visit(ast);

    }
}
