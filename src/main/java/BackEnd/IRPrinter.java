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
        Instruction inst = node.getHeadInst();
        WriteLLVM(node.getLabel() + ":\n");
        this.indentLevel += 1;
        while (inst != node.getTailInst()) {
            WriteLLVM(inst.toString());
            inst = inst.getNext();
        }
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

        BasicBlock head = node.getHeadBlock();

        while (head != node.getTailBlock()) {
            visit(head);
            head = head.getNext();
        }


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
        WriteLLVM("; Module ID = '" + filename + "'\n");
        WriteLLVM("source_filename = " + filename + "\n");
        WriteLLVM("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"\n");


        for ( Function func : node.getFunctionMap().values() ) {
            visit(func);
        }

        return null;
    }

    public void setPrintMode(int printMode) {
        PrintMode = printMode;
    }

}
