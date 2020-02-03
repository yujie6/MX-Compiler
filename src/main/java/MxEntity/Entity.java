package MxEntity;

import Frontend.Scope;

public class Entity {
    private String Identifier;
    Scope scope;

    public Entity(String id) {
        this.Identifier = id;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
