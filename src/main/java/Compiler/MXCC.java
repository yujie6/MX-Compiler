package Compiler;

import AST.MxProgramNode;
import BackEnd.IRBuilder;
import BackEnd.IRPrinter;
import Frontend.ASTBuilder;
import Frontend.GlobalScopeBuilder;
import Frontend.Scope;
import Frontend.SemanticChecker;
import IR.Module;
import Optim.MxOptimizer;
import Tools.MXError;
import Tools.MXLogger;
import Tools.SyntaxErrorListener;
import com.antlr.MxLexer;
import com.antlr.MxParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.logging.Level;




public class MXCC {

    static MXLogger logger;
    private static int debugLevel;
    private static int optimLevel;

    public MXCC() {
        debugLevel = 1;
        optimLevel = 0;
        logger = new MXLogger(getLogLevel());
    }

    private static Level getLogLevel() {
        switch (debugLevel) {
            case 0: {
                return Level.SEVERE;
            }
            case 1: {
                return Level.WARNING;
            }
            case 2: {
                return Level.INFO;
            }
            case 3: {
                return Level.FINE;
            }
            case 4: {
                return Level.FINER;
            }
            default: return Level.SEVERE;
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        Option inputOption = new Option("i", "input", true, "input file path");
        inputOption.setRequired(false);
        options.addOption(inputOption);

        Option outputOption = new Option("o", "output", true, "output file");
        outputOption.setRequired(false);
        options.addOption(outputOption);

        Option helpOption = new Option("h", "help", false, "print this message");
        helpOption.setRequired(false);
        options.addOption(helpOption);

        Option debugOption = new Option("g", "debug", true, "set level of debug information");
        debugOption.setRequired(false);
        options.addOption(debugOption);

        Option semanticOption = new Option("c", "semantic", false, "do not generate ir");
        semanticOption.setRequired(false);
        options.addOption(semanticOption);

        return options;
    }

    public String getGreeting() {
        return "Hello world.";
    }

    public void compile(String fileName) throws IOException {
        CharStream input = null;
        fileName = fileName.replaceAll("\\s","");
        try {
            input = CharStreams.fromFileName(fileName);
        } catch (IOException e) {
            System.err.println("Read File Failed.\n");
        }
        MxProgramNode ast = GetAbstractSyntaxTree(input);
        Scope globalScope = GetGlobalScope(ast);
        SemanticCheck(globalScope, ast);
        Module LLVMTopModule = GetIRModule(ast, globalScope);

        MxOptimizer optimizer = new MxOptimizer(LLVMTopModule, logger);
        optimizer.optimize();

        PrintLLVMIR(LLVMTopModule, "Basic1");
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
        // System.out.println(tree.toStringTree(parser));
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

    private static void PrintLLVMIR(Module irModule, String name) throws IOException {
        IRPrinter printer = new IRPrinter(logger, name);
        printer.setPrintMode(1);
        printer.Print(irModule);
    }

    public static void codegen() throws IOException{
        String homeDirectory = "/home/yujie6/Documents/Compiler/MX-Compiler";
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(homeDirectory));
        builder.command("bash", "codegen.bash");
        Process process = builder.start();

        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert exitCode == 0;

        SequenceInputStream sis = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
        InputStreamReader inst = new InputStreamReader(sis, "GBK");//设置编码格式并转换为输入流
        BufferedReader br = new BufferedReader(inst);//输入流缓冲区

        String res = null;
        StringBuilder sb = new StringBuilder();
        while ((res = br.readLine()) != null) {//循环读取缓冲区中的数据
            sb.append(res+"\n");
        }
        logger.info(sb.toString());
    }

    public static void main(String[] args) throws MXError, IOException {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MX-Compiler", options);

            System.exit(1);
        }

        if (cmd.hasOption("g")) {
            if (cmd.getOptionValue("g") != null) {
                String _level = cmd.getOptionValue("g").replaceAll("\\s", "");
                debugLevel = Integer.parseInt(_level);
            } else {
                debugLevel = 0;
            }
        }
        boolean onlySemantic = false;
        if (cmd.hasOption("c")) {
            onlySemantic = true;
        }

        logger = new MXLogger(getLogLevel());

        if (cmd.hasOption("h")) {
            formatter.printHelp("MX-Compiler", options);
            System.out.println("\ndebug option\tdescription\n" +
                    "-g 0\t\tno debug information\n" +
                    "-g 1\t\tonly warning information\n" +
                    "-g 2\t\twith minimal information\n" +
                    "-g 3\t\twith detail information");
            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String fileName = null;

        if (cmd.hasOption("i")) {
            fileName = inputFilePath.replaceAll("\\s","");
        } else if (args.length == 1) {
            fileName = null;
        }

        CharStream input = null;
        if (fileName == null) {
            InputStream is = System.in;
            input = CharStreams.fromStream(is);
            logger.info("Use stdin as input.");
        } else {
            logger.info("Application start on " + fileName);
            try {
                input = CharStreams.fromFileName(fileName);
            } catch (IOException e) {
                System.err.println("Read File Failed.\n");
            }
        }

        MxProgramNode ast = GetAbstractSyntaxTree(input);
        Scope globalScope = GetGlobalScope(ast);
        SemanticCheck(globalScope, ast);

        if (onlySemantic) {
            return;
        }
        Module LLVMTopModule = GetIRModule(ast, globalScope);
        optimLevel = 1;
        switch (optimLevel) {
            case 0: {
                PrintLLVMIR(LLVMTopModule, "Basic1");
                MxOptimizer optimizer = new MxOptimizer(LLVMTopModule, logger);
                optimizer.optimize();
                PrintLLVMIR(LLVMTopModule, "optim");
                break;
            }
            case 1: {
                MxOptimizer optimizer = new MxOptimizer(LLVMTopModule, logger);
                optimizer.optimize();
                PrintLLVMIR(LLVMTopModule, "Basic1");
                break;
            }
        }
        // codegen part
    }
}