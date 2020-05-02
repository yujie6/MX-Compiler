package BackEnd;

import Target.RVModule;

public abstract class RVPass {
    protected RVModule TopModule;

    public RVPass(RVModule topModule) {
        this.TopModule = topModule;
    }

    public abstract void run();
}
