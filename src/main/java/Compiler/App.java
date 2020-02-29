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
import Tools.MXLogger;
import Tools.SyntaxErrorListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.antlr.MxLexer;
import com.antlr.MxParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class App {

    static MXLogger logger;

    public String getGreeting() {
        return "Hello world.";
    }

    private static MxProgramNode GetAbstractSyntaxTree(CharStream input) {
        MxLexer lexer = new MxLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new SyntaxErrorListener(logger));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        if (logger.getErrorNum() > 0) {
            System.err.println(logger.getErrorNum() + " errors in lexing.");
            System.exit(1);
        }

        MxParser parser = new MxParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener(logger));
        ParseTree tree = parser.mxProgram();

        if (logger.getErrorNum() > 0) {
            System.err.println(logger.getErrorNum() + " errors while parsing.");
            System.exit(1);
        }

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
        IRBuilder irBuilder = new IRBuilder(globalScope, logger);
        return (Module) irBuilder.visit(ast);
    }

    private static void SemanticCheck(Scope globalScope, MxProgramNode ast) {
        (new SemanticChecker(globalScope, logger)).visit(ast);
    }

    private static void PrintLLVMIR(Module irModule) throws IOException {
        IRPrinter printer = new IRPrinter(logger, "Basic1");
        printer.setPrintMode(1);
        printer.Print(irModule);
    }

    public static void main(String[] args) throws MXError, IOException {
        logger = new MXLogger();
        CharStream input = null;
        if (args.length == 1) {
            logger.info("Application start on " + args[0]);
            String fileName = String.valueOf(args[0]);
            try {
                input = CharStreams.fromFileName(fileName);
            } catch (IOException e) {
                System.err.println("Read File Failed.\n");
            }
        } else {
            InputStream is = System.in;
            input = CharStreams.fromStream(is);
            logger.info("Use stdin as input.");
        }

        MxProgramNode ast = GetAbstractSyntaxTree(input);
        Scope globalScope = GetGlobalScope(ast);
        SemanticCheck(globalScope, ast);
        // Module LLVMTopModule = GetIRModule(ast, globalScope);
        //PrintLLVMIR(LLVMTopModule);

    }
}
