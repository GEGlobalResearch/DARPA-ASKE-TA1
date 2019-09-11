package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Node;

public class HowManyValuesContent extends QuestionContent {
	private Node prop;
	private Node typ;
	private String article;
	private Node cls;
	
	
	public HowManyValuesContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public HowManyValuesContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public Node getProp() {
		return prop;
	}

	public void setProp(Node prop) {
		this.prop = prop;
	}

	public Node getTyp() {
		return typ;
	}

	public void setTyp(Node typ) {
		this.typ = typ;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public Node getCls() {
		return cls;
	}

	public void setCls(Node cls) {
		this.cls = cls;
	}
}
