package BackEnd;

import Target.AsmVisitor;
import Target.RVBlock;
import Target.RVFunction;
import Target.RVInstructions.RVInstruction;
import Target.RVModule;
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


    public AsmPrinter(MXLogger logger, String fileName) throws IOException {
        this.logger = logger;
        this.fileName = fileName;
        writer = new FileWriter("/tmp/" + fileName + ".s");
        this.bufw = new BufferedWriter(writer);
        this.indentLevel = 0;
    }


    public void printAssembly(RVModule topModule) throws IOException {
        this.TopModule = topModule;
        visit(topModule);
        bufw.flush();
        logger.info("Assembly print to '/tmp/" + this.fileName + ".s' successfully!");
    }

    @Override
    public Object visit(RVModule rvModule) {
        indentLevel = 1;
        WriteAssembly(".text\n");
        WriteAssembly(".file\t" + this.fileName + ".mx\n");
        for (RVFunction function : rvModule.rvFunctionList) {
            visit(function);
        }
        return null;
    }

    @Override
    public Object visit(RVFunction rvFunction) {
        WriteAssembly(".global\t" + rvFunction.getIdentifier() + "\n");
        WriteAssembly(".p2align\t 2\n");
        WriteAssembly(".type\t" + rvFunction.getIdentifier() + ",@function\n");
        this.indentLevel = 0;
        WriteAssembly(rvFunction.getIdentifier() + ":\n");
        for (RVBlock BB : rvFunction.getRvBlockList()) {
            visit(BB);
        }

        return null;
    }

    @Override
    public Object visit(RVBlock rvBlock) {
        if (!rvBlock.isEntryBlock) {
            WriteAssembly("." + rvBlock.getLabel() + ":\n");
        }
        for (RVInstruction inst : rvBlock.rvInstList) {
            WriteAssembly(inst.toString());
        }

        return null;
    }
}
