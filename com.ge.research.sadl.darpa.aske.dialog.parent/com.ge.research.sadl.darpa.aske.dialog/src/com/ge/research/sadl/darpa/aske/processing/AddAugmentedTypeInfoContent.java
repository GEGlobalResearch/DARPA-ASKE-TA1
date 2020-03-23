package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.dialog.NewExpressionStatement;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.VariableNode;

public class AddAugmentedTypeInfoContent extends AddToModelContent {
	private EquationStatementContent equationContent;		// the Equation being modified
	private NamedNode targetNode;							// the argument of the equation being augmented
	private Node addedType;									// the augmentation information (NamedNode supported for starters)

	public AddAugmentedTypeInfoContent(EObject host) {
		super(host);
	}

	public AddAugmentedTypeInfoContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public AddAugmentedTypeInfoContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public AddAugmentedTypeInfoContent(NewExpressionStatement host, Agent agnt, String uptxt, 
			EquationStatementContent eqContent, NamedNode targetNode, Node addedType) {
		super(host, agnt, uptxt);
		setEquationContent(eqContent);
		setTargetNode(targetNode);
		setAddedType(addedType);
	}

	public Node getAddedType() {
		return addedType;
	}

	public void setAddedType(Node addedType) {
		this.addedType = addedType;
	}

	public NamedNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(NamedNode argument) {
		this.targetNode = argument;
	}

	public EquationStatementContent getEquationContent() {
		return equationContent;
	}

	public void setEquationContent(EquationStatementContent equationContent) {
		this.equationContent = equationContent;
	}


}
