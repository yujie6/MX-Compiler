package BackEnd;

import IR.Argument;
import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;
import IR.Instructions.Instruction;
import IR.Module;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class IRPrinter implements IRVisitor {
    private int PrintMode;
    private Logger logger;
    private Function curFunction;
    private BasicBlock curBasicBlock;


    private FileWriter writer;
    private String filename;
    private BufferedWriter bufw;


    private int indentLevel;
    private String [] _indentMap = {"", "\t", "\t\t", "\t\t\t"};

    private void WriteLLVM(String str) {
        if (str == null) return;
        if (PrintMode == 1) {
            try {
                bufw.write(_indentMap[indentLevel]);
                bufw.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.print(str);
        }
    }

    public IRPrinter(Logger logger, String filename) throws IOException {
        this.logger = logger;
        this.filename = filename;
        writer = new FileWriter("/tmp/" + filename + ".ll");
        this.bufw = new BufferedWriter(writer);
        this.indentLevel = 0;

    }

    public void Print(Module node) throws IOException {
        visit(node);
        bufw.flush();
    }

    @Override
    public Object visit(BasicBlock node) {

        curBasicBlock = node;

        WriteLLVM(node.getLabel() + ":\n");
        this.indentLevel += 1;

        for (Instruction inst : node.getInstList() ) {
            WriteLLVM(inst.toString());
        }

        curBasicBlock = null;
        WriteLLVM("\n");
        this.indentLevel -= 1;
        return null;
    }

    @Override
    public Object visit(Argument node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        WriteLLVM("define dso_local ");
        WriteLLVM(node.getFunctionType().toString() + "{ \n" ) ;
        curFunction = node;
        BasicBlock head = node.getHeadBlock();

        while (head != node.getTailBlock()) {
            visit(head);
            head = head.getNext();
        }

        visit(node.getRetBlock());
        curFunction = null;
        WriteLLVM("}\n");
        return null;
    }

    @Override
    public Object visit(Module node) {
        if (PrintMode == 1) {
            logger.info("Print IR to disk.");
        } else {
            logger.info("Print IR to std out.");
        }
        WriteLLVM("; Module ID = '" + node.ModuleID + "'\n");
        WriteLLVM("source_filename = \"" + node.SourceFileName + "\"\n");
        WriteLLVM("target datalayout = \"" + node.TargetDataLayout + "\"\n");
        WriteLLVM("target triple = \"" + node.TargetTriple + "\"\n");


        for ( Function func : node.getFunctionMap().values() ) {
            visit(func);
        }

        return null;
    }

    public void setPrintMode(int printMode) {
        PrintMode = printMode;
    }

}
