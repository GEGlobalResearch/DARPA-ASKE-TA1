package com.research.ge.darpa.aske.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OutputInfo
{
	private String sentence;
	private int sentID;
	// XmLElementWrapper generates a wrapper element around XML representation
	@XmlElementWrapper(name = "conceptList")
	// XmlElement sets the name of the entities
	@XmlElement(name = "concept")
	private List<Concept> conceptInfoList;
	
	public OutputInfo()
	{
		
	}
	
	public void addConceptInfo(Concept info)
	{
		if (this.conceptInfoList == null)
			this.conceptInfoList = new ArrayList<Concept>();
		this.conceptInfoList.add(info);
	}
	
	public void setConceptInfoList(List<Concept> infoList)
	{
		this.conceptInfoList = infoList;
	}
	
	public List<Concept> getConceptInfoList()
	{
		return this.conceptInfoList;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	@Override
	public String toString() {
		return "OutputInfo [sentence=" + sentence + ", conceptInfoList="
				+ conceptInfoList + "]";
	}

	public int getSentID() {
		return sentID;
	}

	public void setSentID(int sentID) {
		this.sentID = sentID;
	}
}
