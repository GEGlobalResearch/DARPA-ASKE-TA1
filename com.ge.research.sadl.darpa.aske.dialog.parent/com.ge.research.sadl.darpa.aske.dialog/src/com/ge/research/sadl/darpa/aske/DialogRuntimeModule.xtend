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
package com.ge.research.sadl.darpa.aske

import com.ge.research.sadl.ValueConverterService
import com.ge.research.sadl.darpa.aske.processing.DialogModelProcessorProvider
import com.ge.research.sadl.darpa.aske.scoping.DialogErrorAddingLinkingService
import com.ge.research.sadl.generator.SADLOutputConfigurationProvider
import com.ge.research.sadl.processing.IModelProcessorProvider
import com.ge.research.sadl.scoping.SadlQualifiedNameConverter
import com.ge.research.sadl.scoping.SadlQualifiedNameProvider
import com.ge.research.sadl.scoping.SilencedImportedNamesAdapter
import com.ge.research.sadl.validation.ResourceValidator
import com.ge.research.sadl.validation.SoftLinkingMessageProvider
import com.google.inject.Binder
import com.google.inject.Singleton
import java.io.IOException
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.generator.IOutputConfigurationProvider
import org.eclipse.xtext.linking.impl.DefaultLinkingService
import org.eclipse.xtext.linking.impl.ImportedNamesAdapter
import org.eclipse.xtext.linking.impl.LinkingDiagnosticMessageProvider
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.parsetree.reconstr.IParseTreeConstructor
import org.eclipse.xtext.parsetree.reconstr.ITokenStream
import org.eclipse.xtext.validation.ResourceValidatorImpl

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class DialogRuntimeModule extends AbstractDialogRuntimeModule {
		
	override bindIQualifiedNameProvider() {	//same
		SadlQualifiedNameProvider
	}
	
	override configure (Binder binder) {	// same
		super.configure(binder);
		binder.bind(IOutputConfigurationProvider).to(SADLOutputConfigurationProvider).in(Singleton);
	}
	
//	def Class<? extends DefaultResourceDescriptionManager> bindDefaultResourceDescriptionManager() {
//		return SadlResourceDescriptionManager;
//	}
	
//	def Class<? extends IEObjectDocumentationProvider> bindIEObjectDocumentationProvider() {
//		return SadlEObjectDocumentationProvider;
//	}
	
	def Class<? extends IQualifiedNameConverter> bindIQualifiedNameCoverter() {		// same
		return SadlQualifiedNameConverter;
	}
	
	def Class<? extends LinkingDiagnosticMessageProvider> bindILinkingDiagnosticMessageProvider() {	// same
		SoftLinkingMessageProvider
	}
	
	override bindIValueConverterService() {	// same
		ValueConverterService
	}
	
	def Class<? extends ResourceValidatorImpl> bindResourceValidatorImpl() {	// same
		return ResourceValidator
	}
	
	def Class<? extends IModelProcessorProvider> bindIModelProcessorProvider() {	// similar
		return DialogModelProcessorProvider
//		return JenaBasedDialogModelProcessor
	}
	
// this is what's in SADL and SRL	
	def Class<? extends DefaultLinkingService> bindDefaultLinkingService() {	// same
		return DialogErrorAddingLinkingService;
	}
	
	def Class<? extends IParseTreeConstructor> bindIParseTreeConstructor() {	// same
		NoImplParseTreeConstructor
	}
	
//	def Class<? extends SadlMarkerLocationProvider> bindSadlMarkerLocationProvider() {	// no customization needed?
//		RequirementsMarkerLocationProvider
//	}
	
	static class NoImplParseTreeConstructor implements IParseTreeConstructor {	// same
		
		override serializeSubtree(EObject object, ITokenStream out) throws IOException {
			throw new UnsupportedOperationException("TODO: auto-generated method stub")
		}
		
	}
	
//	def Class<? extends DefaultResourceDescriptionStrategy> bindResourceDescritpionStrategy() {
//		return SadlResourceDescriptionStrategy;
//	}
// This is what it is for SRL
//	def Class<? extends DefaultResourceDescriptionStrategy> bindResourceDescritpionStrategy() {
//		return ResourceDescriptionStrategy
//	}
	
//	def Class<? extends IDeclarationExtensionsContribution> bindIDeclarationExtensionsContribution() {
//    	return RequirementsDeclarationExtensionsContribution;
//  	}
	
	def Class<? extends ImportedNamesAdapter> bindImportedNamesAdapter() {
		return SilencedImportedNamesAdapter; 
	}
	
}
