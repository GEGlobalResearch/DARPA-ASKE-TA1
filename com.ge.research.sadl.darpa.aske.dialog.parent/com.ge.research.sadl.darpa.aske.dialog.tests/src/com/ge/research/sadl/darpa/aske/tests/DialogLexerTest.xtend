package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.darpa.aske.parser.antlr.lexer.InternalDialogLexer
import com.google.inject.Inject
import com.google.inject.Provider
import org.antlr.runtime.ANTLRStringStream
import org.antlr.runtime.Token
import org.eclipse.xtext.parser.antlr.Lexer
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.Test
import org.junit.runner.RunWith

import static java.lang.System.lineSeparator
import static com.ge.research.sadl.darpa.aske.parser.antlr.internal.InternalDialogParser.*
import static org.junit.Assert.assertEquals

@RunWith(XtextRunner)
@InjectWith(DialogInjectorProvider)
class DialogLexerTest {

	@Inject
	Provider<InternalDialogLexer> lexer

	@Test
	def void eval_0_params() {
		val it = '''
			uri "http://test".
			Evaluate CAL_SOS().
		'''.createLexer
		assertNextToken(Uri, 'uri')
		assertNextToken(RULE_WS, ' ')
		assertNextToken(RULE_STRING, '"http://test"')
		assertNextToken(FullStop, '.')
		assertNextToken(RULE_WS, lineSeparator)
		assertNextToken(Evaluate, 'Evaluate')
		assertNextToken(RULE_WS, ' ')
		assertNextToken(RULE_ID, 'CAL_SOS')
		assertNextToken(LeftParenthesis, '(')
		assertNextToken(RightParenthesis, ')')
		assertNextToken(FullStop, '.')
		assertNextToken(RULE_WS, lineSeparator)
		nextToken.assertEndOfTokens
	}

	@Test
	def void eval_2_params() {
		val it = '''
			uri "http://test".
			Evaluate CAL_SOS(1, 2).
		'''.createLexer
		assertNextToken(Uri, 'uri')
		assertNextToken(RULE_WS, ' ')
		assertNextToken(RULE_STRING, '"http://test"')
		assertNextToken(FullStop, '.')
		assertNextToken(RULE_WS, lineSeparator)
		assertNextToken(Evaluate, 'Evaluate')
		assertNextToken(RULE_WS, ' ')
		assertNextToken(RULE_ID, 'CAL_SOS')
		assertNextToken(LeftParenthesis, '(')
		assertNextToken(RULE_NUMBER, '1')
		assertNextToken(Comma, ',')
		assertNextToken(RULE_WS, ' ')
		assertNextToken(RULE_NUMBER, '2')
		assertNextToken(RightParenthesis, ')')
		assertNextToken(FullStop, '.')
		assertNextToken(RULE_WS, lineSeparator)
		nextToken.assertEndOfTokens
	}

	private def createLexer(CharSequence cs) {
		return lexer.get => [
			charStream = new ANTLRStringStream(cs.toString)
		]
	}

	private def assertEndOfTokens(Token token) {
		token.assertEquals(-1, null)
	}

	private def void assertNextToken(Lexer it, int id, String text) {
		nextToken.assertEquals(id, text)
	}

	private def void assertEquals(Token token, int id, String text) {
		assertEquals('''Expected token text was: «text», actual was: «token.text»''', id, token.type)
		if (text !== null) {
			assertEquals('''Expected token was: «id»''', text, token.text)
		}
	}
}
