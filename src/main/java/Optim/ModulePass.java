package Optim;

import IR.Module;

public abstract class ModulePass extends Pass {
    protected Module TopModule;
    public ModulePass(Module module) {
        this.TopModule = module;
    }
}
