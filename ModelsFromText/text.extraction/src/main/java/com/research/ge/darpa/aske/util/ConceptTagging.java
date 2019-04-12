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

package com.research.ge.darpa.aske.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConceptTagging {
	
	private String nlpServiceURL = "http://vesuvius-test.crd.ge.com:9080/kcud/";
	private static Dictionary dictionary;
	
	public ConceptTagging(String ontologyFile) throws IOException {
		dictionary = new Dictionary();
		List<String> owlLineList = new ArrayList<String>();		
		BufferedReader b = new BufferedReader(new FileReader(ontologyFile));
		String l = "";
		while ((l = b.readLine()) != null)
			owlLineList.add(l);
		b.close();
		dictionary.setOwlLineList(owlLineList);
	}
	
	public List<String> runConceptTagging(String text) throws Exception {
		String nlpConceptTaggingServiceURL = nlpServiceURL + "/conceptTaggingJSON";
		
		URL connectionURL = new URL(nlpConceptTaggingServiceURL);
		HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		
		Gson gson = new Gson();
		InputInfo inputInfo = new InputInfo();
		inputInfo.addDictObj(dictionary);
		inputInfo.addParagraph(text);		
		inputInfo.setTaxonomyDatasetURL(null);
		
		String input = gson.toJson(inputInfo, InputInfo.class);
			
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(input.getBytes());
		outputStream.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String responseJSonString = "";
		String line = "";
		while ((line = br.readLine()) != null)
			responseJSonString += line;
		
	
		Type listType = new TypeToken<List<OutputInfo>>() {}.getType();
		List<OutputInfo> outputInfoList = new Gson().fromJson(responseJSonString, listType);		
		return conceptTaggingHelper(outputInfoList);
	}
	
	private List<String> conceptTaggingHelper(List<OutputInfo> outputInfoList) {
		List<String> taggedConcepts = new ArrayList<String>();
		if(outputInfoList != null){
			for(OutputInfo outInfo : outputInfoList) {
				List<Concept> concepts = outInfo.getConceptInfoList();
				for(Concept concept : concepts) {
					String conceptMapperTag = concept.getName().trim();
					if(conceptMapperTag != null)
						taggedConcepts.add(conceptMapperTag);
				}
			}
		}
		return taggedConcepts;
	}

}
