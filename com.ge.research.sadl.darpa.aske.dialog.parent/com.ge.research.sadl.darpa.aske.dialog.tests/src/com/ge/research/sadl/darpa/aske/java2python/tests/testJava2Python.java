package com.ge.research.sadl.darpa.aske.java2python.tests;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.junit.Test;

import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;

public class testJava2Python {

	@Test
	public void test() throws MalformedURLException, UnsupportedEncodingException {
		AnswerExtractionProcessor ep = new AnswerExtractionProcessor(null, null);
		String code = "  public double CAL_SOS (double T, double G, double R, double Q) {\n" + 
				"      double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) *\n" + 
				"         Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));\n" + 
				"      return (Math.sqrt(32.174 * T * R * WOW));\n" + 
				"  }\n" + 
				"";
		ep.translateMethodJavaToPython("Mach", code);
	}

}
