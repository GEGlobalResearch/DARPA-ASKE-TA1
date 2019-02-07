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
