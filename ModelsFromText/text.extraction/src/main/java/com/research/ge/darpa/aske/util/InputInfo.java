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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class InputInfo
{
	@XmlElementWrapper(name = "dictObjList")
	@XmlElement (name = "Dictionary")
	private List<Dictionary> dictObjList;
	@XmlElementWrapper(name = "paragraphList")
	@XmlElement (name = "Paragraph")
	private List<String> paragraphList;
	private boolean lemmatize;
	private String taxonomyDatasetURL;

	public List<String> getParagraphList() {
		return paragraphList;
	}

	public void addParagraph(String paragraph)
	{
		if (this.paragraphList == null)
			this.paragraphList = new ArrayList<String>();
		this.paragraphList.add(paragraph);
	}
	
	public List<Dictionary> getDictObjList()
	{
		return dictObjList;
	}

	public void addDictObj(Dictionary dictObj)
	{
		if (this.dictObjList == null)
			this.dictObjList = new ArrayList<Dictionary>();
		this.dictObjList.add(dictObj);
	}
	
	public void setLemmatize(boolean lemmatize)
	{
		this.lemmatize = lemmatize;
	}
	
	public boolean isLemmatize()
	{
		return this.lemmatize;
	}

	@Override
	public String toString() {
		return "InputInfo [dictObjList=" + dictObjList 
				+ ", paragraphList=" + paragraphList + "]";
	}
	
	public String getTaxonomyDatasetURL() {
		return taxonomyDatasetURL;
	}

	public void setTaxonomyDatasetURL(String taxonomyDatasetURL) {
		this.taxonomyDatasetURL = taxonomyDatasetURL;
	}
	
}
