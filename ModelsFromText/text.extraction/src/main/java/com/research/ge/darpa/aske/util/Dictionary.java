package com.research.ge.darpa.aske.util;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Dictionary")
public class Dictionary
{
	private List<String> owlLineList;
	private List<String> uimaDictLineList;
	
	private String matcherConfigFilename;
	
	private String entityType;
	
	public Dictionary()
	{
		
	}

	public List<String> getOwlLineList() {
		return owlLineList;
	}

	public void setOwlLineList(List<String> owlLineList) {
		this.owlLineList = owlLineList;
	}

	public List<String> getUimaDictLineList() {
		return uimaDictLineList;
	}

	public void setUimaDictLineList(List<String> uimaDictLineList) {
		this.uimaDictLineList = uimaDictLineList;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getMatcherConfigFilename() {
		return matcherConfigFilename;
	}

	public void setMatcherConfigFilename(String matcherConfigFilename) {
		this.matcherConfigFilename = matcherConfigFilename;
	}

	@Override
	public String toString() {
		return "Dictionary [entityType=" + entityType + "]";
	}
}
