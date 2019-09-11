package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Node;

public class WhatValuesContent extends QuestionContent {
	private String typeof = null;
	private Node prop = null;
	private String article = null;
	private Node cls = null;
	
	public WhatValuesContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public WhatValuesContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public Node getCls() {
		return cls;
	}

	public void setCls(Node cls) {
		this.cls = cls;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public Node getProp() {
		return prop;
	}

	public void setProp(Node node) {
		this.prop = node;
	}

	public String getTypeof() {
		return typeof;
	}

	public void setTypeof(String typeof) {
		this.typeof = typeof;
	}

	public String toString() {
		ICompositeNode nd = NodeModelUtils.findActualNodeFor(getHostEObject().eContainer());
		return nd.getText().trim();
	}
}
