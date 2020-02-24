/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Compiler;

import AST.ASTNode;
import AST.MxProgramNode;
import BackEnd.IRBuilder;
import BackEnd.IRPrinter;
import Frontend.*;

import IR.GlobalVariable;
import IR.Module;
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

    static Logger logger;

    public String getGreeting() {
        return "Hello world.";
    }

    private static Logger GetMXLogger() {
        Logger logger = Logger.getLogger("MXLogger");
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LogFormatter());
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);
        return logger;
    }

    private static MxProgramNode GetAbstractSyntaxTree(CharStream input) {
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

    private static Scope GetGlobalScope(MxProgramNode ast) {
        GlobalScopeBuilder gsbuilder = new GlobalScopeBuilder(logger);
        gsbuilder.visit(ast);
        logger.info("GlobalScope build smoothly");
        return gsbuilder.getGlobalScope();
    }

    private static Module GetIRModule(MxProgramNode ast, Scope globalScope) {
        (new SemanticChecker(globalScope, logger)).visit(ast);
        IRBuilder irBuilder = new IRBuilder(globalScope, logger);
        return (Module) irBuilder.visit(ast);
    }

    private static void PrintLLVMIR(Module irModule) throws IOException {
        IRPrinter printer = new IRPrinter(logger, "Basic1");
        printer.setPrintMode(1);
        printer.Print(irModule);
    }

    public static void main(String[] args) throws MXError, IOException {
        // int[][] a = new int[2][];
        // Setting logger
        logger = GetMXLogger();
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

        MxProgramNode ast = GetAbstractSyntaxTree(input);
        Scope globalScope = GetGlobalScope(ast);
        Module LLVMTopModule = GetIRModule(ast, globalScope);
        PrintLLVMIR(LLVMTopModule);

    }
}
