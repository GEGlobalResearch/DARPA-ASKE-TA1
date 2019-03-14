package com.ge.research.sadl.darpa.aske.scoping;

import static com.ge.research.sadl.scoping.AmbiguousNameErrorEObjectDescription.AMBIGUOUS_NAME_ALTERNATIVES;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IEObjectDescription;

import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.dialog.HowManyValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.ModifiedAskStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatStatement;
import com.ge.research.sadl.scoping.AmbiguousNameErrorEObjectDescription;
import com.ge.research.sadl.scoping.ErrorAddingLinkingService;

public class DialogErrorAddingLinkingService extends ErrorAddingLinkingService {

	/**
	 * This tests to see if the alternatives are all the same. If they are then it is not
	 * ambiguous as concepts with the same URI are the same concept. This can happen when 
	 * multiple projects are used, each with their own sadlimplicitmodel.
	 * @param eObjectDescription
	 * @return
	 */
	protected boolean alternativesAllSame(IEObjectDescription eObjectDescription) {
		if (eObjectDescription instanceof AmbiguousNameErrorEObjectDescription) {
			boolean ignore = true;
			Iterator<IEObjectDescription> altsitr = ((AmbiguousNameErrorEObjectDescription)eObjectDescription).getAllDescriptions().iterator();
			while (altsitr.hasNext()) {
				IEObjectDescription alt = altsitr.next();
				if (alt instanceof AmbiguousNameErrorEObjectDescription) {
					Iterator<IEObjectDescription> alteobjitr = ((AmbiguousNameErrorEObjectDescription)alt).getEObjDescAlternatives().iterator();
					while (alteobjitr.hasNext()) {
						EObject alteobj = alteobjitr.next().getEObjectOrProxy();
						if (EcoreUtil2.getContainerOfType(alteobj, AnswerCMStatement.class) == null &&
								EcoreUtil2.getContainerOfType(alteobj, ModifiedAskStatement.class) == null &&
								EcoreUtil2.getContainerOfType(alteobj, WhatStatement.class) == null &&
								EcoreUtil2.getContainerOfType(alteobj, HowManyValuesStatement.class) == null) {
							ignore = false;
							break;
						}
					}
				}
			}
			if (ignore) {
				return true;
			}
		}
		String alternatives = eObjectDescription.getUserData(AMBIGUOUS_NAME_ALTERNATIVES);
		StringTokenizer st = new StringTokenizer(alternatives, ",");
		String lastToken = null;
		boolean allSame = true;
		while (st.hasMoreTokens()) {
			String thisToken = st.nextToken();
			if (lastToken != null && !lastToken.equals(thisToken)) {
				allSame = false;
				break;
			}
			lastToken = thisToken;
		}
		return allSame;
	}

}
