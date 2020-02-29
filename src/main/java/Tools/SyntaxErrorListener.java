package Tools;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class SyntaxErrorListener extends BaseErrorListener {
    private MXLogger logger;

    public SyntaxErrorListener(MXLogger logger) {
        this.logger = logger;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {
        // super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
        logger.severe(msg, new Location(line, charPositionInLine));
    }
}
