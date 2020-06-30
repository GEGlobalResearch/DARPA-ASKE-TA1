/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Project: ANSWER, developed with the support of the Defense Advanced 
 * Research Projects Agency (DARPA) under Agreement  No.  HR00111990006. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************/
package com.ge.research.sadl.darpa.aske.java2python.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.reasoner.ConfigurationException;

public class testJava2Python {

	private String domainProjectModelFolder;
	private String extractionProjectModelFolder;

	@Before
	public void setUp() throws Exception {
		String codeExtractionKbRoot = "resources/M5Snapshot";
		File codeExtractionPrjFolder = new File(codeExtractionKbRoot);
		assertTrue(codeExtractionPrjFolder.exists());
		setExtractionProjectModelFolder(codeExtractionPrjFolder.getCanonicalPath() + "/OwlModels");
		
		setDomainProjectModelFolder(codeExtractionPrjFolder.getCanonicalPath() + "/OwlModels");
	}

	@Ignore("can only be run if the translation service is running at the default service URL")
	@Test
	public void test() throws IOException, ConfigurationException {
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		AnswerExtractionProcessor ep = new AnswerExtractionProcessor(acm, null);
		String code = "  public double CAL_SOS (double T, double G, double R, double Q) {\n" + 
				"      double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) *\n" + 
				"         Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));\n" + 
				"      return (Math.sqrt(32.174 * T * R * WOW));\n" + 
				"  }";
		String pythoncode = ep.translateMethodJavaToPython("Mach", code);
		System.out.println(pythoncode);
	}

	private String getExtractionProjectModelFolder() {
		return extractionProjectModelFolder;
	}

	private void setExtractionProjectModelFolder(String extractionProjectModelFolder) {
		this.extractionProjectModelFolder = extractionProjectModelFolder;
	}

	private String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	private void setDomainProjectModelFolder(String outputProjectModelFolder) {
		this.domainProjectModelFolder = outputProjectModelFolder;
	}
}
