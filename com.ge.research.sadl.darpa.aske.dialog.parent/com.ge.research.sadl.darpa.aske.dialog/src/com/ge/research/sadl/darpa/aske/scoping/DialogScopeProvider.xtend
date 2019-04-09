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

package com.ge.research.sadl.darpa.aske.scoping

import com.ge.research.sadl.scoping.SADLScopeProvider
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import com.ge.research.sadl.sADL.ExpressionScope
import org.eclipse.xtext.EcoreUtil2
import com.ge.research.sadl.sADL.SadlModel

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class DialogScopeProvider extends SADLScopeProvider {

	override protected getSadlResourceScope(EObject obj, EReference reference) {
		val parent = createResourceScope(obj.eResource, null, newHashSet);
		val statement = EcoreUtil2.getContainerOfType(obj, ExpressionScope)
		if (statement !== null) {
			val model = EcoreUtil2.getContainerOfType(statement, SadlModel)
			var newParent = parent
//			for (context : model.elements.filter(RequirementContext)) {
//				if (context !== statement) {
//					newParent = getLocalVariableScope(#[context.expr], newParent)
//				}
//			}
//			if (statement instanceof RequirementContext) {
//				if (statement.expr !== null) {
//					newParent = getLocalVariableScope(#[statement.expr], newParent)
//				}
//			}
//			if (statement instanceof WherePart) {
//				if (statement.where !== null) {
//					newParent = getLocalVariableScope(#[statement.where], newParent)
//				}
//			}
//			if (statement instanceof WithWhenPart) {
//				if (statement.when !== null) {
//					newParent = getLocalVariableScope(#[statement.when], newParent);
//					if (obj.eContainer instanceof BinaryOperation) {
//						val container = obj.eContainer as BinaryOperation;
//						if ((container.op == 'is' || container.op == '=') && container.left == obj) {
//							val importedNamespace = converter.toQualifiedName(statement.name.concreteName);
//							newParent = newParent.doWrap(importedNamespace);
//						}
//					}
//				}
//			}
			return newParent
		}
		return super.getSadlResourceScope(obj, reference)
	}

}
