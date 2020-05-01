package BackEnd;

import Target.RVModule;

public abstract class RVPass {
    private RVModule TopModule;

    public RVPass(RVModule topModule) {
        this.TopModule = topModule;
    }

    public abstract void run();
}
