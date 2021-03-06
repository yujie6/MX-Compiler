package BackEnd;

import IR.*;
import IR.Instructions.*;
import IR.Module;
import IR.Types.StructureType;
import Tools.MXLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class IRPrinter implements IRVisitor {
    private int PrintMode;
    private boolean isAssignLabel;
    private MXLogger logger;
    private Function curFunction;
    private BasicBlock curBasicBlock;
    private int ValueID;
    private String filename;

    private FileWriter writer;
    private BufferedWriter bufw;

    private int indentLevel;
    private String[] _indentMap = {"", "\t", "\t\t", "\t\t\t"};

    private void WriteLLVM(String str) {
        if (str == null) return;
        if (PrintMode >= 1) {
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

    public IRPrinter(MXLogger logger, String filename) throws IOException {
        this.logger = logger;
        this.filename = filename;
        writer = new FileWriter("/tmp/" + filename + ".ll");
        this.bufw = new BufferedWriter(writer);
        this.indentLevel = 0;
        this.ValueID = 0;
    }

    public void Print(Module node) throws IOException {
        isAssignLabel = true;
        visit(node);
        isAssignLabel = false;
        visit(node);
        bufw.flush();
        logger.info("IR print to '/tmp/" + this.filename + ".ll' successfully!");
    }

    @Override
    public Object visit(BasicBlock node) {
        if (isAssignLabel) {

            for (Instruction inst : node.getInstList()) {
                // br store and call void has no register
                if (inst.getRegisterID() != null) {
                    if (inst instanceof CallInst) {
                        if (!((CallInst) inst).isVoid()) {
                            inst.setRegisterID("%" + ValueID);
                            ValueID += 1;
                        }
                    } else {
                        inst.setRegisterID("%" + ValueID);
                        ValueID += 1;
                    }
                }
            }
        } else {
            curBasicBlock = node;
            WriteLLVM(node.getLabel() + ":  ;" + node.getIdentifier() + "\n");
            this.indentLevel += 1;
            for (Instruction inst : node.getInstList()) {
                if (PrintMode == 2 && inst instanceof PhiInst) {
                    continue;
                }
                WriteLLVM(inst.toString());
            }

            curBasicBlock = null;
            WriteLLVM("\n");
            this.indentLevel -= 1;
        }
        return null;
    }


    @Override
    public Object visit(Function node) {
        this.ValueID = 0;
        if (isAssignLabel) {
            this.ValueID += node.getParameterList().size();
            /*BasicBlock head = node.getHeadBlock();
            while (head != node.getTailBlock()) {
                if (head.getInstList().size() != 0) {
                    head.setLabel(String.valueOf(ValueID));
                    ValueID += 1;
                    visit(head);
                }
                head = head.getNext();
            }*/
            for (BasicBlock BB : node.getBlockList()) {
                if (BB.getInstList().size() != 0 && BB != node.getRetBlock()) {
                    BB.setLabel(String.valueOf(ValueID));
                    ValueID += 1;
                    visit(BB);
                }
            }
            node.getRetBlock().setLabel(String.valueOf(ValueID));
            ValueID += 1;
            visit(node.getRetBlock());
        } else {

            WriteLLVM("define dso_local ");
            WriteLLVM(node.getFunctionType().toString() + "{ \n");
            curFunction = node;
            /*BasicBlock head = node.getHeadBlock();
            while (head != node.getTailBlock()) {
                if (head.getInstList().size() != 0) {
                    visit(head);
                }
                head = head.getNext();
            }*/
            for (BasicBlock BB : node.getBlockList()) {
                if (BB.getInstList().size() != 0 && BB != node.getRetBlock()) {
                    visit(BB);
                }
            }
            visit(node.getRetBlock());
            curFunction = null;
            WriteLLVM("}\n");
        }
        return null;
    }

    @Override
    public Object visit(AllocaInst allocaInst) {
        return null;
    }

    @Override
    public Object visit(BinOpInst binOpInst) {
        return null;
    }

    @Override
    public Object visit(BitCastInst bitCastInst) {
        return null;
    }

    @Override
    public Object visit(BranchInst branchInst) {
        return null;
    }

    @Override
    public Object visit(CallInst callInst) {
        return null;
    }

    @Override
    public Object visit(CmpInst cmpInst) {
        return null;
    }

    @Override
    public Object visit(CopyInst copyInst) {
        return null;
    }

    @Override
    public Object visit(GetPtrInst getPtrInst) {
        return null;
    }

    @Override
    public Object visit(LoadInst loadInst) {
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        return null;
    }

    @Override
    public Object visit(PhiInst phiInst) {
        return null;
    }

    @Override
    public Object visit(SextInst sextInst) {
        return null;
    }

    @Override
    public Object visit(StoreInst storeInst) {
        return null;
    }


    public Object visit(Module node) {
        if (PrintMode >= 1 && isAssignLabel) {
            logger.info("Print IR to \"" + filename + ".ll\"");
        } else if (PrintMode == 0 && isAssignLabel){
            logger.info("Print IR to std out.");
        }
        if (!isAssignLabel) {
            WriteLLVM("; Module ID = '" + node.ModuleID + "'\n");
            WriteLLVM("source_filename = \"" + node.SourceFileName + "\"\n");
            WriteLLVM("target datalayout = \"" + node.TargetDataLayout + "\"\n");
            WriteLLVM("target triple = \"" + node.TargetTriple + "\"\n\n");

            for (StructureType classType : node.getClassMap().values()) {
                WriteLLVM(classType.getDeclaration() + "\n");
            }

            for (Value gvar : node.getGlobalVarMap().values()) {
                WriteLLVM( ((GlobalVariable) gvar).toString());
            }

        }



        for (Function func : node.getFunctionMap().values()) {
            if (func.isExternal()) {
                if (!isAssignLabel) WriteLLVM(func.toString() + "\n\n"); // only write declare
            } else visit(func);
        }

        return null;
    }

    public void setPrintMode(int printMode) {
        PrintMode = printMode;
    }

}
