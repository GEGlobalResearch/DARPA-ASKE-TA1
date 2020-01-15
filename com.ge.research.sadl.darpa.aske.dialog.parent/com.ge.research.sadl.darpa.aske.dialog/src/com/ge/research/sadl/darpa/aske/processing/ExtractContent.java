package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class ExtractContent extends ExpectsAnswerContent {
	private String url = null;
	private String scheme = null;
	private String source = null;
	
	public ExtractContent(EObject host) {
		super(host);
	}

	public ExtractContent(EObject host, Agent agnt) {
		super(host, agnt);
	}
	
	public ExtractContent(EObject host, Agent agnt, String scheme, String source, String url) {
		super(host, agnt);
		setScheme(scheme);
		setSource(source);
		setUrl(url);
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
