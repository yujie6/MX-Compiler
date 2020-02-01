// Generated from /home/yujie6/Music/Compiler/MX-Compiler/src/main/antlr/LexerRules.g4 by ANTLR 4.8
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LexerRules extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		INT=1, BOOL=2, STRING=3, VOID=4, IF=5, ELSE=6, FOR=7, WHILE=8, BREAK=9, 
		CONTINUE=10, RETURN=11, NEW=12, CLASS=13, THIS=14, STRING_LITERAL=15, 
		BOOL_LITERAL=16, NULL_LITERAL=17, DECIMAL_LITERAL=18, IDENTIFIER=19, WS=20, 
		COMMENT=21, LINE_COMMENT=22;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"INT", "BOOL", "STRING", "VOID", "IF", "ELSE", "FOR", "WHILE", "BREAK", 
			"CONTINUE", "RETURN", "NEW", "CLASS", "THIS", "STRING_LITERAL", "BOOL_LITERAL", 
			"NULL_LITERAL", "DECIMAL_LITERAL", "IDENTIFIER", "WS", "COMMENT", "LINE_COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'int'", "'bool'", "'string'", "'void'", "'if'", "'else'", "'for'", 
			"'while'", "'break'", "'continue'", "'return'", "'new'", "'class'", "'this'", 
			null, null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "INT", "BOOL", "STRING", "VOID", "IF", "ELSE", "FOR", "WHILE", 
			"BREAK", "CONTINUE", "RETURN", "NEW", "CLASS", "THIS", "STRING_LITERAL", 
			"BOOL_LITERAL", "NULL_LITERAL", "DECIMAL_LITERAL", "IDENTIFIER", "WS", 
			"COMMENT", "LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public LexerRules(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "LexerRules.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\30\u00c7\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\7\20\u0080\n\20\f\20"+
		"\16\20\u0083\13\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\5\21\u0090\n\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\7\23\u0099\n\23"+
		"\f\23\16\23\u009c\13\23\3\23\5\23\u009f\n\23\3\24\3\24\7\24\u00a3\n\24"+
		"\f\24\16\24\u00a6\13\24\3\25\6\25\u00a9\n\25\r\25\16\25\u00aa\3\25\3\25"+
		"\3\26\3\26\3\26\3\26\7\26\u00b3\n\26\f\26\16\26\u00b6\13\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\27\3\27\3\27\3\27\7\27\u00c1\n\27\f\27\16\27\u00c4\13"+
		"\27\3\27\3\27\3\u00b4\2\30\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30\3\2\n\6\2"+
		"\f\f\17\17$$^^\5\2$$^^pp\3\2\63;\3\2\62;\5\2C\\aac|\6\2\62;C\\aac|\5\2"+
		"\13\f\16\17\"\"\4\2\f\f\17\17\2\u00cf\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2"+
		"\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3"+
		"\2\2\2\2+\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2\5\63\3\2\2\2\78\3\2\2\2\t?\3\2"+
		"\2\2\13D\3\2\2\2\rG\3\2\2\2\17L\3\2\2\2\21P\3\2\2\2\23V\3\2\2\2\25\\\3"+
		"\2\2\2\27e\3\2\2\2\31l\3\2\2\2\33p\3\2\2\2\35v\3\2\2\2\37{\3\2\2\2!\u008f"+
		"\3\2\2\2#\u0091\3\2\2\2%\u009e\3\2\2\2\'\u00a0\3\2\2\2)\u00a8\3\2\2\2"+
		"+\u00ae\3\2\2\2-\u00bc\3\2\2\2/\60\7k\2\2\60\61\7p\2\2\61\62\7v\2\2\62"+
		"\4\3\2\2\2\63\64\7d\2\2\64\65\7q\2\2\65\66\7q\2\2\66\67\7n\2\2\67\6\3"+
		"\2\2\289\7u\2\29:\7v\2\2:;\7t\2\2;<\7k\2\2<=\7p\2\2=>\7i\2\2>\b\3\2\2"+
		"\2?@\7x\2\2@A\7q\2\2AB\7k\2\2BC\7f\2\2C\n\3\2\2\2DE\7k\2\2EF\7h\2\2F\f"+
		"\3\2\2\2GH\7g\2\2HI\7n\2\2IJ\7u\2\2JK\7g\2\2K\16\3\2\2\2LM\7h\2\2MN\7"+
		"q\2\2NO\7t\2\2O\20\3\2\2\2PQ\7y\2\2QR\7j\2\2RS\7k\2\2ST\7n\2\2TU\7g\2"+
		"\2U\22\3\2\2\2VW\7d\2\2WX\7t\2\2XY\7g\2\2YZ\7c\2\2Z[\7m\2\2[\24\3\2\2"+
		"\2\\]\7e\2\2]^\7q\2\2^_\7p\2\2_`\7v\2\2`a\7k\2\2ab\7p\2\2bc\7w\2\2cd\7"+
		"g\2\2d\26\3\2\2\2ef\7t\2\2fg\7g\2\2gh\7v\2\2hi\7w\2\2ij\7t\2\2jk\7p\2"+
		"\2k\30\3\2\2\2lm\7p\2\2mn\7g\2\2no\7y\2\2o\32\3\2\2\2pq\7e\2\2qr\7n\2"+
		"\2rs\7c\2\2st\7u\2\2tu\7u\2\2u\34\3\2\2\2vw\7v\2\2wx\7j\2\2xy\7k\2\2y"+
		"z\7u\2\2z\36\3\2\2\2{\u0081\7$\2\2|\u0080\n\2\2\2}~\7^\2\2~\u0080\t\3"+
		"\2\2\177|\3\2\2\2\177}\3\2\2\2\u0080\u0083\3\2\2\2\u0081\177\3\2\2\2\u0081"+
		"\u0082\3\2\2\2\u0082\u0084\3\2\2\2\u0083\u0081\3\2\2\2\u0084\u0085\7$"+
		"\2\2\u0085 \3\2\2\2\u0086\u0087\7v\2\2\u0087\u0088\7t\2\2\u0088\u0089"+
		"\7w\2\2\u0089\u0090\7g\2\2\u008a\u008b\7h\2\2\u008b\u008c\7c\2\2\u008c"+
		"\u008d\7n\2\2\u008d\u008e\7u\2\2\u008e\u0090\7g\2\2\u008f\u0086\3\2\2"+
		"\2\u008f\u008a\3\2\2\2\u0090\"\3\2\2\2\u0091\u0092\7p\2\2\u0092\u0093"+
		"\7w\2\2\u0093\u0094\7n\2\2\u0094\u0095\7n\2\2\u0095$\3\2\2\2\u0096\u009a"+
		"\t\4\2\2\u0097\u0099\t\5\2\2\u0098\u0097\3\2\2\2\u0099\u009c\3\2\2\2\u009a"+
		"\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u009f\3\2\2\2\u009c\u009a\3\2"+
		"\2\2\u009d\u009f\7\62\2\2\u009e\u0096\3\2\2\2\u009e\u009d\3\2\2\2\u009f"+
		"&\3\2\2\2\u00a0\u00a4\t\6\2\2\u00a1\u00a3\t\7\2\2\u00a2\u00a1\3\2\2\2"+
		"\u00a3\u00a6\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5(\3"+
		"\2\2\2\u00a6\u00a4\3\2\2\2\u00a7\u00a9\t\b\2\2\u00a8\u00a7\3\2\2\2\u00a9"+
		"\u00aa\3\2\2\2\u00aa\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00ac\3\2"+
		"\2\2\u00ac\u00ad\b\25\2\2\u00ad*\3\2\2\2\u00ae\u00af\7\61\2\2\u00af\u00b0"+
		"\7,\2\2\u00b0\u00b4\3\2\2\2\u00b1\u00b3\13\2\2\2\u00b2\u00b1\3\2\2\2\u00b3"+
		"\u00b6\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b5\u00b7\3\2"+
		"\2\2\u00b6\u00b4\3\2\2\2\u00b7\u00b8\7,\2\2\u00b8\u00b9\7\61\2\2\u00b9"+
		"\u00ba\3\2\2\2\u00ba\u00bb\b\26\2\2\u00bb,\3\2\2\2\u00bc\u00bd\7\61\2"+
		"\2\u00bd\u00be\7\61\2\2\u00be\u00c2\3\2\2\2\u00bf\u00c1\n\t\2\2\u00c0"+
		"\u00bf\3\2\2\2\u00c1\u00c4\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2"+
		"\2\2\u00c3\u00c5\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c5\u00c6\b\27\2\2\u00c6"+
		".\3\2\2\2\f\2\177\u0081\u008f\u009a\u009e\u00a4\u00aa\u00b4\u00c2\3\2"+
		"\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}