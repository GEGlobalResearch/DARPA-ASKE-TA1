package com.ge.research.sadl.darpa.aske.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;

public class OwlToSadlModelTest {

	@Test
	public void test() throws IOException, OwlImportException {
		String owlFilePath = "resources/M5Snapshot/ExtractedModels/Mach.java.saveForTest.owl";
		File owlFile = new File(owlFilePath);
		assertTrue(owlFile.exists());
		System.out.println(owlFile.getAbsolutePath());
		OwlToSadl o2s = new OwlToSadl(owlFile);
		o2s.setVerboseMode(true);
		String sadlModelContent = o2s.getSadlModel();
		String expected = "";
		System.out.println(sadlModelContent);
	}

}
