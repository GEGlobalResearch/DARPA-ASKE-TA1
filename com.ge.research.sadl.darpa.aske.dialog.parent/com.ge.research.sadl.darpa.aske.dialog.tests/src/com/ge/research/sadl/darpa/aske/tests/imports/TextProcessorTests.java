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
		tp.process(null, "a^2 = R * T * gamma", null);
	}

	@Test
	public void test2() throws MalformedURLException, UnsupportedEncodingException {
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, "The speed of sound is a concept known in physics ", null);
	}

}
