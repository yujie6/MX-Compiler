package BackEnd;

import Target.*;
import Target.RVInstructions.RVInstruction;
import Tools.MXLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AsmPrinter implements AsmVisitor {

    private RVModule TopModule;
    private MXLogger logger;
    private String fileName;
    private FileWriter writer;
    private BufferedWriter bufw;
    private String[] _indentMap = {"", "\t", "\t\t", "\t\t\t"};
    private int indentLevel;

    private void WriteAssembly(String str) {
        if (str == null) return;
        try {
            bufw.write(_indentMap[indentLevel]);
            bufw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public AsmPrinter(MXLogger logger, String filePath) throws IOException {
        this.logger = logger;
        this.fileName = filePath;
        writer = new FileWriter(filePath);
        this.bufw = new BufferedWriter(writer);
        this.indentLevel = 0;
    }


    public void printAssembly(RVModule topModule) throws IOException {
        this.TopModule = topModule;
        visit(topModule);
        bufw.flush();
        logger.info("Assembly print to '" + this.fileName + "' successfully!");
    }

    @Override
    public Object visit(RVModule rvModule) {
        indentLevel = 1;
        WriteAssembly("\t.text\n");
        WriteAssembly("\t.file\t\"" + this.fileName.replaceAll(".mx", ".c") + "\"\n");
        for (RVFunction function : rvModule.rvFunctions) {
            visit(function);
        }
        WriteAssembly(".section\t.sdata,\"aw\",@progbits\n");
        // .data is a read-write section containing global or static variables
        for (RVGlobal rvGlobal : rvModule.rvGlobals) {
            if (!rvGlobal.isStringConst) {
                WriteAssembly(rvGlobal.toString());
            }
        }

        // WriteAssembly("\t.section\t.rodata.str1.1,\"aMS\",@progbits,1\n");
        // .rodata is a read-only section containing const variables
        for (int i = rvModule.rvGlobals.size() - 1; i >= 0; i--) {
            RVGlobal rvGlobal = rvModule.rvGlobals.get(i);
            if (rvGlobal.isStringConst) {
                WriteAssembly(rvGlobal.toString());
            }
        }
        return null;
    }

    private int functionNum = 0;

    @Override
    public Object visit(RVFunction rvFunction) {

        WriteAssembly("\t.globl\t" + rvFunction.getIdentifier() +
                "\t\t\t# -- Begin function " + rvFunction.getIdentifier() + "\n");
        WriteAssembly("\t.p2align\t 2\n");
        WriteAssembly("\t.type\t" + rvFunction.getIdentifier() + ",@function\n");
        this.indentLevel = 0;
        WriteAssembly(rvFunction.getIdentifier() + ":\n");
        for (RVBlock BB : rvFunction.getRvBlockList()) {
            visit(BB);
        }
//        WriteAssembly(".Lfunc_end" + functionNum + ":\n");
//
//        WriteAssembly("\t.size\t" + rvFunction.getIdentifier() + ",\t" + ".Lfunc_end" + functionNum +
//                "-" + rvFunction.getIdentifier() + "\n");
//        functionNum += 1;
        WriteAssembly("\t\t\t\t\t\t\t# -- End function\n");

        return null;
    }

    @Override
    public Object visit(RVBlock rvBlock) {
        if (!rvBlock.isEntryBlock) {
            WriteAssembly(rvBlock.getLabel() + ":\n");
        }
        for (RVInstruction inst : rvBlock.rvInstList) {
            WriteAssembly("\t");
            WriteAssembly(inst.toString());
        }

        return null;
    }
}
