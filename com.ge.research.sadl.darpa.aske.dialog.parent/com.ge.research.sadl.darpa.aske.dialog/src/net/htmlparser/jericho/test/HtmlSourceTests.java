package net.htmlparser.jericho.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.ge.research.sadl.reasoner.utils.SadlUtils;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;

class HtmlSourceTests {

	@Test
	void test_01() {
		String content =
				"<HTML>\r\n" + 
				"<HEAD>\r\n" + 
				"<TITLE>Your Title Here</TITLE>\r\n" + 
				"</HEAD>\r\n" + 
				"<BODY BGCOLOR=\"FFFFFF\">\r\n" + 
				"<HR>\r\n" + 
				"<a href=\"http://somegreatsite.com\">Link\n" + 
				"Name</a>\r\n" + 
				"is a link to another nifty site\r\n" + 
				"<P> This is a new paragraph!\r\n" + 
				"<P> <B>This is a new paragraph!</B>\r\n" + 
				"<BR> <B><I>This is a new sentence without a\n" + 
				"paragraph break, in bold italics.</I></B>\r\n" + 
				"<HR>\r\n" + 
				"</BODY>\r\n" + 
				"</HTML>";
		String result = new Source(content).getRenderer().toString();
		System.out.println(result);
	}

	@Test
	void test_02() throws IOException {
		File file = new File("C:\\TMP\\IsentropicFlowEquations.html");
		if (!file.exists()) {
			fail();
		}
		String content = (new SadlUtils()).fileToString(file);
		Source src = new Source(content);
		Renderer rndrr = src.getRenderer();
		rndrr.setNewLine("\n");
		rndrr.setIncludeHyperlinkURLs(false);
		rndrr.setConvertNonBreakingSpaces(true);
		String result = rndrr.toString();
		System.out.println(result);
	}
}
