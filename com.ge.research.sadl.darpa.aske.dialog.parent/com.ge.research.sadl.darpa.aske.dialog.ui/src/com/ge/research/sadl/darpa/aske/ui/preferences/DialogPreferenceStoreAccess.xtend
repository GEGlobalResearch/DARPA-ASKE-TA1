/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
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
package com.ge.research.sadl.darpa.aske.ui.preferences

import com.ge.research.sadl.darpa.aske.dialog.ui.internal.DialogActivator
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Singleton
import com.google.inject.name.Names
import java.util.Collection
import org.eclipse.xtext.Constants
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer
import org.eclipse.xtext.ui.editor.preferences.PreferenceStoreAccessImpl
import org.slf4j.LoggerFactory

/**
 * Preference store access to share a singleton instance between multiple DSLs.
 * 
 * @author akos.kitta
 */
@Singleton
class DialogPreferenceStoreAccess extends PreferenceStoreAccessImpl {
	
	static val LOGGER = LoggerFactory.getLogger(DialogPreferenceStoreAccess);
	
	val Collection<String> initializedLanguages;
	
	@Inject
	Injector injector;
	
	new() {
		this.initializedLanguages = newHashSet();
		LOGGER.info('''Creating preference store access: �this�.''');
	}
	
	@Override
	override toString() {
		return '''Shared Dialog preference store access [�System.identityHashCode(this)�]''';
	}
	
	@Override
	override setLanguageNameAsQualifier(String languageName) {
		// Noop. This is to be able to share the preference store access between multiple languages.
	}
	
	@Override
	override protected getQualifier() {
		return DialogActivator.COM_GE_RESEARCH_SADL_DARPA_ASKE_DIALOG;
	}
	
	@Override
	override protected lazyInitialize() {
		// Do the initialization per language.
		initializeForLanguage(injector);
	}
	
	@Inject
	package def void initializeForLanguage(Injector anInjector) {
		// Do the initialization per language.
		val languageId = anInjector.getInstance(Key.get(String, Names.named(Constants.LANGUAGE_NAME)));
		if (languageId !== null && !initializedLanguages.contains(languageId)) {
			synchronized (this) {
				if (!initializedLanguages.contains(languageId)) {
					// First register it as initialized, otherwise we run into an endless loop.
					initializedLanguages.add(languageId);
					anInjector.initializer.initialize(this);
				}
			}
		}
	}
	
	private def getInitializer(Injector injector) {
		return injector.getInstance(IPreferenceStoreInitializer.CompositeImpl);
	}
	
}
