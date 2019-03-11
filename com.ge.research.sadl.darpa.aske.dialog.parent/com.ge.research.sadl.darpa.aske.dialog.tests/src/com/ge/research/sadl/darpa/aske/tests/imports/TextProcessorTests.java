package com.ge.research.sadl.darpa.aske.tests.imports;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;

public class TextProcessorTests {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws MalformedURLException, UnsupportedEncodingException {
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, null, null);
	}

}
