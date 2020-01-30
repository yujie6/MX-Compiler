package Tools;

import org.antlr.v4.runtime.*;

public class Location {
    private int line;
    private int column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Location(Token token) {
        this.line = token.getLine();                        // line is 1 - n;
        this.column = token.getCharPositionInLine() + 1;    // pos is 0 - n-1;
    }

    public Location(ParserRuleContext ctx) {
        this.line = ctx.getStart().getLine();
        this.column = ctx.getStart().getCharPositionInLine() + 1;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int compare(Location other) {
        if (line < other.getLine())
            return -1;
        else if (line > other.getLine())
            return 1;
        else return Integer.compare(column, other.getColumn());
    }

    @Override
    public String toString() {
        String position = String.format("%d:%d", this.line, this.column);
        return position;
    }
}
