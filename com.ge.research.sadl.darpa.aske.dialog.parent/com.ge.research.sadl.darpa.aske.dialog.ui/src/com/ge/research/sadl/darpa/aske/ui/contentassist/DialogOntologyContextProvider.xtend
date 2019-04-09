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
