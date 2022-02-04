/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
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
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;

import org.apache.jena.ontology.OntModel;

public interface IModelFromCodeExtractor {

	String getPackageName();

	void addCodeFile(File javaFile);

	void addCodeFiles(List<File> javaFiles);
	
	List<File> getCodeFiles();
	
	void setCodeFiles(List<File> codeFiles);

	IConfigurationManagerForIDE getCodeModelConfigMgr();

	boolean process(String inputIdentifier, String content, String modelName, String modelPrefix) throws ConfigurationException, IOException, TranslationException;

	String getCodeModelName();
	
	void setCodeModelName(String modelName);
	
	String getCodeModelPrefix();
	
	void setCodeModelPrefix(String prefix);

	String getCodeModelNamespace();

	void addCodeModel(String key, OntModel codeModel);

	Map<String,OntModel> getCodeModels();

	OntModel getCodeModel(String key);
	
	void setCurrentCodeModel(OntModel m);
	
	OntModel getCurrentCodeModel();

	boolean isIncludeSerialization();

	void setIncludeSerialization(boolean includeSerialization);

	ResultSet executeSparqlQuery(String query) throws ConfigurationException, ReasonerNotFoundException, IOException,
			InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException;

	String[] extractPythonEquationFromCodeExtractionModel(String pythonScript);

//	String[] extractPythonTFEquationFromCodeExtractionModel(String pythonScript);
//
//	String[] extractPythonTFEquationFromCodeExtractionModel(String pythonScript, String defaultMethodName);
//
	String[] extractPythonEquationFromCodeExtractionModel(String pythonScript, String defaultMethodName);

	String getClassUriFromSimpleName(String name);

	/**
	 * Method to return the aggregated comments from the code extracted
	 * @return
	 */
	String getAggregatedComments();

	/**
	 * Method to clear previous aggregated comments
	 */
	void clearAggregatedComments();

}