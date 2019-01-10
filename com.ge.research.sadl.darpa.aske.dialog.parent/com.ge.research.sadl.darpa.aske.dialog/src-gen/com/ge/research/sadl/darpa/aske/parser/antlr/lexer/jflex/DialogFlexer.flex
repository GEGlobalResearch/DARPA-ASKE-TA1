/*
 * generated by Xtext 2.14.0.RC1
 */
package com.ge.research.sadl.darpa.aske.parser.antlr.lexer.jflex;

import java.io.Reader;
import java.io.IOException;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.TokenSource;

import static com.ge.research.sadl.darpa.aske.parser.antlr.internal.InternalDialogParser.*;

@SuppressWarnings({"all"})
%%

%{
	public final static TokenSource createTokenSource(Reader reader) {
		return new DialogFlexer(reader);
	}

	private int offset = 0;
	
	public void reset(Reader reader) {
		yyreset(reader);
		offset = 0;
	}

	@Override
	public Token nextToken() {
		try {
			int type = advance();
			if (type == Token.EOF) {
				return Token.EOF_TOKEN;
			}
			int length = yylength();
			final String tokenText = yytext();
			CommonToken result = new CommonTokenWithText(tokenText, type, Token.DEFAULT_CHANNEL, offset);
			offset += length;
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getSourceName() {
		return "FlexTokenSource";
	}

	public static class CommonTokenWithText extends CommonToken {

		private static final long serialVersionUID = 1L;

		public CommonTokenWithText(String tokenText, int type, int defaultChannel, int offset) {
			super(null, type, defaultChannel, offset, offset + tokenText.length() - 1);
			this.text = tokenText;
		}
	}

%}

%unicode
%implements org.antlr.runtime.TokenSource
%class DialogFlexer
%function advance
%public
%int
%eofval{
return Token.EOF;
%eofval}

WS=[\ \n\r\t]+
ANY_OTHER=.

QNAME_TERMINAL= {ID} ":" {ID}

ID="^"? [:jletter:] {ID_PART}* ("." {ID_PART}+)*
ID_PART= [:jletterdigit:] | "-" | "%" | "~"


ML_COMMENT="/*" ~"*/"
COMMENT_ERROR_PATTERN="/*" [^*]* ("*"+ [^/*] [^*]*)* "*"?
SL_COMMENT="/""/"[^\r\n]*(\r?\n)?

NUMBER = {DIGIT}+ ("." {DIGIT}+)? (("e"|"E") ("+"|"-")? {DIGIT}+)?
		|("." {DIGIT}+) (("e"|"E") ("+"|"-")? {DIGIT}+)?
DIGIT = [0-9]

STRING=("'"([^\\\']|{ESCAPE_SEQUENCE})*"'"?)|(\"([^\\\"]|{ESCAPE_SEQUENCE})*\"?)
ESCAPE_SEQUENCE=\\{ANY_OTHER}


%%

<YYINITIAL> "nonNegativeInteger" { return NonNegativeInteger; }
<YYINITIAL> "nonPositiveInteger" { return NonPositiveInteger; }
<YYINITIAL> "negativeInteger" { return NegativeInteger; }
<YYINITIAL> "positiveInteger" { return PositiveInteger; }
<YYINITIAL> "anySimpleType" { return AnySimpleType; }
<YYINITIAL> "base64Binary" { return Base64Binary; }
<YYINITIAL> "relationship" { return Relationship; }
<YYINITIAL> "unsignedByte" { return UnsignedByte; }
<YYINITIAL> "symmetrical" { return Symmetrical; }
<YYINITIAL> "unsignedInt" { return UnsignedInt; }
<YYINITIAL> "Deductions" { return Deductions; }
<YYINITIAL> "annotation" { return Annotation; }
<YYINITIAL> "gYearMonth" { return GYearMonth; }
<YYINITIAL> "transitive" { return Transitive; }
<YYINITIAL> "construct" { return Construct; }
<YYINITIAL> "described" { return Described; }
<YYINITIAL> "describes" { return Describes; }
<YYINITIAL> "gMonthDay" { return GMonthDay; }
<YYINITIAL> "hexBinary" { return HexBinary; }
<YYINITIAL> "instances" { return Instances; }
<YYINITIAL> "top-level" { return TopLevel; }
<YYINITIAL> "Equation" { return Equation; }
<YYINITIAL> "Explain:" { return Explain; }
<YYINITIAL> "External" { return External; }
<YYINITIAL> "contains" { return Contains; }
<YYINITIAL> "dateTime" { return DateTime; }
<YYINITIAL> "disjoint" { return Disjoint; }
<YYINITIAL> "distinct" { return Distinct; }
<YYINITIAL> "duration" { return Duration; }
<YYINITIAL> "matching" { return Matching; }
<YYINITIAL> "property" { return Property; }
<YYINITIAL> "another" { return Another; }
<YYINITIAL> "boolean" { return Boolean; }
<YYINITIAL> "classes" { return Classes; }
<YYINITIAL> "contain" { return Contain; }
<YYINITIAL> "decimal" { return Decimal; }
<YYINITIAL> "default" { return Default; }
<YYINITIAL> "element" { return Element; }
<YYINITIAL> "exactly" { return Exactly; }
<YYINITIAL> "integer" { return Integer; }
<YYINITIAL> "inverse" { return Inverse; }
<YYINITIAL> "located" { return Located; }
<YYINITIAL> "returns" { return Returns; }
<YYINITIAL> "seventh" { return Seventh; }
<YYINITIAL> "subject" { return Subject; }
<YYINITIAL> "sublist" { return Sublist; }
<YYINITIAL> "version" { return Version; }
<YYINITIAL> "Print:" { return Print; }
<YYINITIAL> "Update" { return Update; }
<YYINITIAL> "Write:" { return Write; }
<YYINITIAL> "always" { return Always; }
<YYINITIAL> "anyURI" { return AnyURI; }
<YYINITIAL> "before" { return Before; }
<YYINITIAL> "delete" { return Delete; }
<YYINITIAL> "double" { return Double; }
<YYINITIAL> "eighth" { return Eighth; }
<YYINITIAL> "exists" { return Exists; }
<YYINITIAL> "fourth" { return Fourth; }
<YYINITIAL> "gMonth" { return GMonth; }
<YYINITIAL> "import" { return Import; }
<YYINITIAL> "insert" { return Insert; }
<YYINITIAL> "length" { return Length; }
<YYINITIAL> "return" { return Return; }
<YYINITIAL> "second" { return Second; }
<YYINITIAL> "select" { return Select; }
<YYINITIAL> "single" { return Single; }
<YYINITIAL> "string" { return String; }
<YYINITIAL> "unique" { return Unique; }
<YYINITIAL> "values" { return Values; }
<YYINITIAL> "Expr:" { return Expr; }
<YYINITIAL> "Graph" { return Graph; }
<YYINITIAL> "Model" { return Model; }
<YYINITIAL> "Read:" { return Read; }
<YYINITIAL> "Stage" { return Stage; }
<YYINITIAL> "Test:" { return Test; }
<YYINITIAL> "after" { return After; }
<YYINITIAL> "alias" { return Alias; }
<YYINITIAL> "class" { return Class; }
<YYINITIAL> "count" { return Count; }
<YYINITIAL> "false" { return False; }
<YYINITIAL> "fifth" { return Fifth; }
<YYINITIAL> "first" { return First; }
<YYINITIAL> "float" { return Float; }
<YYINITIAL> "gYear" { return GYear; }
<YYINITIAL> "given" { return Given; }
<YYINITIAL> "graph" { return Graph_1; }
<YYINITIAL> "index" { return Index; }
<YYINITIAL> "known" { return Known; }
<YYINITIAL> "least" { return Least; }
<YYINITIAL> "level" { return Level; }
<YYINITIAL> "ninth" { return Ninth; }
<YYINITIAL> "order" { return Order; }
<YYINITIAL> "other" { return Other; }
<YYINITIAL> "sixth" { return Sixth; }
<YYINITIAL> "tenth" { return Tenth; }
<YYINITIAL> "there" { return There; }
<YYINITIAL> "third" { return Third; }
<YYINITIAL> "types" { return Types; }
<YYINITIAL> "using" { return Using; }
<YYINITIAL> "value" { return Value; }
<YYINITIAL> "where" { return Where; }
<YYINITIAL> "List" { return List; }
<YYINITIAL> "None" { return None; }
<YYINITIAL> "Rule" { return Rule; }
<YYINITIAL> "What" { return What; }
<YYINITIAL> "byte" { return Byte; }
<YYINITIAL> "data" { return Data; }
<YYINITIAL> "date" { return Date; }
<YYINITIAL> "desc" { return Desc; }
<YYINITIAL> "does" { return Does; }
<YYINITIAL> "from" { return From; }
<YYINITIAL> "gDay" { return GDay; }
<YYINITIAL> "have" { return Have; }
<YYINITIAL> "last" { return Last; }
<YYINITIAL> "long" { return Long; }
<YYINITIAL> "many" { return Many; }
<YYINITIAL> "most" { return Most; }
<YYINITIAL> "must" { return Must; }
<YYINITIAL> "note" { return Note; }
<YYINITIAL> "only" { return Only; }
<YYINITIAL> "same" { return Same; }
<YYINITIAL> "some" { return Some; }
<YYINITIAL> "then" { return Then; }
<YYINITIAL> "time" { return Time; }
<YYINITIAL> "true" { return True; }
<YYINITIAL> "type" { return Type; }
<YYINITIAL> "what" { return What_1; }
<YYINITIAL> "with" { return With; }
<YYINITIAL> "..." { return FullStopFullStopFullStop; }
<YYINITIAL> "Ask" { return Ask; }
<YYINITIAL> "CM:" { return CM; }
<YYINITIAL> "How" { return How; }
<YYINITIAL> "The" { return The; }
<YYINITIAL> "and" { return And; }
<YYINITIAL> "any" { return Any; }
<YYINITIAL> "are" { return Are; }
<YYINITIAL> "asc" { return Asc; }
<YYINITIAL> "ask" { return Ask_1; }
<YYINITIAL> "can" { return Can; }
<YYINITIAL> "for" { return For; }
<YYINITIAL> "has" { return Has; }
<YYINITIAL> "how" { return How_1; }
<YYINITIAL> "int" { return Int; }
<YYINITIAL> "not" { return Not; }
<YYINITIAL> "one" { return One; }
<YYINITIAL> "the" { return The_1; }
<YYINITIAL> "uri" { return Uri; }
<YYINITIAL> "!=" { return ExclamationMarkEqualsSign; }
<YYINITIAL> "&&" { return AmpersandAmpersand; }
<YYINITIAL> "--" { return HyphenMinusHyphenMinus; }
<YYINITIAL> "<=" { return LessThanSignEqualsSign; }
<YYINITIAL> "==" { return EqualsSignEqualsSign; }
<YYINITIAL> "=>" { return EqualsSignGreaterThanSign; }
<YYINITIAL> ">=" { return GreaterThanSignEqualsSign; }
<YYINITIAL> "An" { return An; }
<YYINITIAL> "PI" { return PI; }
<YYINITIAL> "an" { return An_1; }
<YYINITIAL> "as" { return As; }
<YYINITIAL> "at" { return At; }
<YYINITIAL> "be" { return Be; }
<YYINITIAL> "by" { return By; }
<YYINITIAL> "if" { return If; }
<YYINITIAL> "in" { return In; }
<YYINITIAL> "is" { return Is; }
<YYINITIAL> "of" { return Of; }
<YYINITIAL> "or" { return Or; }
<YYINITIAL> "to" { return To; }
<YYINITIAL> "||" { return VerticalLineVerticalLine; }
<YYINITIAL> "!" { return ExclamationMark; }
<YYINITIAL> "%" { return PercentSign; }
<YYINITIAL> "(" { return LeftParenthesis; }
<YYINITIAL> ")" { return RightParenthesis; }
<YYINITIAL> "*" { return Asterisk; }
<YYINITIAL> "+" { return PlusSign; }
<YYINITIAL> "," { return Comma; }
<YYINITIAL> "-" { return HyphenMinus; }
<YYINITIAL> "." { return FullStop; }
<YYINITIAL> "/" { return Solidus; }
<YYINITIAL> ":" { return Colon; }
<YYINITIAL> "<" { return LessThanSign; }
<YYINITIAL> "=" { return EqualsSign; }
<YYINITIAL> ">" { return GreaterThanSign; }
<YYINITIAL> "?" { return QuestionMark; }
<YYINITIAL> "A" { return A; }
<YYINITIAL> "E" { return E; }
<YYINITIAL> "[" { return LeftSquareBracket; }
<YYINITIAL> "]" { return RightSquareBracket; }
<YYINITIAL> "^" { return CircumflexAccent; }
<YYINITIAL> "a" { return A_1; }
<YYINITIAL> "e" { return E_1; }
<YYINITIAL> "{" { return LeftCurlyBracket; }
<YYINITIAL> "}" { return RightCurlyBracket; }



<YYINITIAL> {COMMENT_ERROR_PATTERN} { return 0; /* antlr <invalid> */ }

<YYINITIAL> {NUMBER} { return RULE_NUMBER; }
<YYINITIAL> {WS} { return RULE_WS; }
<YYINITIAL> {ID} { return RULE_ID; }
<YYINITIAL> {QNAME_TERMINAL} { return RULE_QNAME_TERMINAL; }
<YYINITIAL> {STRING} { return RULE_STRING; }
<YYINITIAL> {ML_COMMENT} { return RULE_ML_COMMENT; }
<YYINITIAL> {SL_COMMENT} { return RULE_SL_COMMENT; }
<YYINITIAL> {ANY_OTHER} { return RULE_ANY_OTHER; }
