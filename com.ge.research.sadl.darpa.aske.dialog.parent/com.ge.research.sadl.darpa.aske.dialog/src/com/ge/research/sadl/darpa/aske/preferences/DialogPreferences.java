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
package com.ge.research.sadl.darpa.aske.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.xtext.preferences.PreferenceKey;

@SuppressWarnings("restriction")
public class DialogPreferences {
	public static final PreferenceKey ORIGINAL_LANGUAGE = new PreferenceKey("saveOriginalScript", String.valueOf(true));
	public static final PreferenceKey PYTHON_LANGUAGE = new PreferenceKey("savePythonScript", String.valueOf(true));
	public static final PreferenceKey OTHER_PYTHON_LANGUAGE = new PreferenceKey("saveOtherPytonScript", String.valueOf(true));
	public static final PreferenceKey ANSWER_TEXT_SERVICE_BASE_URI = new PreferenceKey("textServiceBaseUri", "http://localhost:4200");
	public static final PreferenceKey ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI = new PreferenceKey("java2pythonServiceBaseUri", "http://localhost:19092");
	public static final PreferenceKey USE_ANSWER_KCHAIN_CG_SERVICE = new PreferenceKey("useKChainCgService", String.valueOf(false));
	public static final PreferenceKey ANSWER_KCHAIN_CG_SERVICE_BASE_URI = new PreferenceKey("kChainCgServiceBaseUri", "http://localhost:12345");
	public static final PreferenceKey USE_DBN_CG_SERVICE = new PreferenceKey("useDbnCgService", String.valueOf(false));
	public static final PreferenceKey ANSWER_DBN_CG_SERVICE_BASE_URI = new PreferenceKey("dbnCgServiceBaseUri", "http://localhost:5000");
	public static final PreferenceKey DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI = new PreferenceKey("DbnInputJsonGenerationServiceBaseUri", "http://localhost:46000");
	public static final PreferenceKey ANSWER_INVIZIN_SERVICE_BASE_URI = new PreferenceKey("InvizinServiceBaseUri", "http://localhost:12309");
	public static final PreferenceKey ANSWER_CODE_EXTRACTION_KBASE_ROOT = new PreferenceKey("codeExtractionKbaseRoot", "resources/CodeModel");
	public static final PreferenceKey SHORT_GRAPH_LINK = new PreferenceKey("shortGraphLink", "");
	
	private static final PreferenceKey[] allKeys = {
			ORIGINAL_LANGUAGE,
			PYTHON_LANGUAGE,
			OTHER_PYTHON_LANGUAGE,
			ANSWER_TEXT_SERVICE_BASE_URI,
			ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI,
			USE_ANSWER_KCHAIN_CG_SERVICE,
			ANSWER_KCHAIN_CG_SERVICE_BASE_URI,
			USE_DBN_CG_SERVICE,
			ANSWER_DBN_CG_SERVICE_BASE_URI,
			DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI,
			ANSWER_INVIZIN_SERVICE_BASE_URI,
			ANSWER_CODE_EXTRACTION_KBASE_ROOT,
			SHORT_GRAPH_LINK
	};

	public static final List<PreferenceKey> preferences() {
		return Arrays.asList(allKeys);
	}
}
