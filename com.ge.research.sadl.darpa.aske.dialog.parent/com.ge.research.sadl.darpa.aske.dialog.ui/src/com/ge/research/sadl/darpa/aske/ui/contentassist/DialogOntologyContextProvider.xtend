/*
 * © 2014-2017 General Electric Company – All Rights Reserved
 * 
 * This software and any accompanying data and documentation are CONFIDENTIAL 
 * INFORMATION of the General Electric Company (“GE”) and may contain trade secrets 
 * and other proprietary information.  It is intended for use solely by GE and authorized 
 * personnel.
 */
package com.ge.research.sadl.darpa.aske.ui.contentassist

import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement
import com.ge.research.sadl.ide.editor.contentassist.SadlOntologyContextProvider
import com.ge.research.sadl.processing.IModelProcessor
import com.ge.research.sadl.processing.ISadlOntologyHelper.Context
import com.ge.research.sadl.processing.ValidationAcceptor
import com.google.common.base.Optional
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext

import static com.ge.research.sadl.processing.ISadlOntologyHelper.GrammarContextIds.*

/**
 * Ontology context provider for SRL. Delegates into the SADL context provider.
 * If the delegate one cannot handle the context to context transformation, this
 * service should handle the SRL specific cases.
 * 
 * @author akos.kitta
 */
class DialogOntologyContextProvider extends SadlOntologyContextProvider {
	
	override Optional<Context> getOntologyContext(ContentAssistContext it, IModelProcessor processor,
		ValidationAcceptor acceptor) {
		val curmodel = currentModel
		if (curmodel instanceof AnswerCMStatement) {
			
		}
		else {
			val context = super.getOntologyContext(it, processor, acceptor)
			if (context.present && context.get.grammarContextId == PROPOFSUBJECT_RIGHT) {
				
			}
			if (context.present) {
				return context;
			}
		}
		return Optional.absent;
	}
	
	
}
