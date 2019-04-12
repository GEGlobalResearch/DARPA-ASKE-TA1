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
public class Concept implements Comparable<Concept>
{
	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "childConceptList")
	// XmlElement sets the name of the entities
	@XmlElement(name = "concept")
	private List<Concept> childConceptList;
	private String name;
	private String semanticType;
	private String uriStr;
	private int startByte;
	private int endByte;
	
	public Concept(String name)
	{
		this.name = name;
		this.semanticType = "";
		this.uriStr = "";
	}
	
	public Concept(String name, int startByte, int endByte)
	{
		this.name = name;
		this.startByte = startByte;
		this.endByte = endByte;
		this.semanticType = "";
		this.uriStr = "";
	}
	
	public void setSemanticType(String type)
	{
		this.semanticType = type;
	}
	
	public void setUriStr(String id)
	{
		this.uriStr = id;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getSemanticType()
	{
		return this.semanticType;
	}
	
	public String getUriStr()
	{
		return this.uriStr;
	}
	
	public int getStartByte()
	{
		return this.startByte;
	}
	
	public int getEndByte()
	{
		return this.endByte;
	}
	
	public void addChildConcept(Concept childConcept)
	{
		if (this.childConceptList == null)
			this.childConceptList = new ArrayList<Concept>();
		this.childConceptList.add(childConcept);
	}
	
	public List<Concept> getChildConceptList()
	{
		return this.childConceptList;
	}
	
	public String toString()
	{
		String str = this.name + "|" + this.semanticType + "|" + this.uriStr + "|" + this.startByte + "|" + this.endByte;
		return str;
	}
	
	public boolean equals(Object other)
	{
		boolean isEqual = false;

		if (this == other)
			return true;
		if ( !(other instanceof Concept) )
			return false;

		Concept obj = (Concept) other;
		if (this.name.equalsIgnoreCase(obj.name) && this.semanticType.equals(obj.semanticType))
			isEqual = true;

		return isEqual;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 1;
		
		hash = hash * 17 + name.hashCode();
		hash = hash * 13 + startByte;
		hash = hash * 23 + endByte;

		return hash;
	}

	public int compareTo(Concept other)
	{
		int retValue = 0;
		
		if (other.endByte <= this.startByte)
			retValue = -1;
		if (other.startByte >= this.endByte)
			retValue = 1;
		
		return retValue;
	}
	
//	public int compareTo(Concept other)
//	{
//		String name1 = this.getName().toUpperCase();
//	    String name2 = other.getName().toUpperCase();
//	    
//	    //ascending order
//	    return name1.compareTo(name2);
//	}

}
