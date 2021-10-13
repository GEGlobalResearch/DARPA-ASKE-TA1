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
package com.ge.research.sadl.darpa.aske.ui.preferences;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage;

import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.model.persistence.SadlPersistenceFormat;
import com.ge.research.sadl.reasoner.ConfigurationItem;
import com.ge.research.sadl.reasoner.ConfigurationItem.NameValuePair;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.ui.preferences.FieldEditorExtensions.BooleanFieldEditorExt;
import com.ge.research.sadl.ui.preferences.FieldEditorExtensions.FieldEditorExt;
import com.ge.research.sadl.ui.preferences.FieldEditorExtensions.StringFieldEditorExt;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

public class DialogRootPreferencePage extends LanguageRootPreferencePage {
	/**
	 * You can iterate on this, but <b>DO NOT</b> modify it!</br>
	 * It is a hack for https://github.com/eclipse/xtext-eclipse/issues/916.
	 */
	protected List<FieldEditor> editorsCopy = Lists.newArrayList();

	@Override
	public void dispose() {
		super.dispose();
		editorsCopy.clear();
	}

	@Override
	protected void addField(FieldEditor editor) {
		super.addField(editor);
		// We have to save our copy of the field editors.
		// Otherwise, we cannot perform a custom `updateFieldEditors(boolean)`.
		// `editors` is private in the super class.
		this.editorsCopy.add(editor);
	}
	
	@Override
	protected void adjustGridLayout() {
		Iterable<FieldEditorExt> fieldExts = FluentIterable.from(editorsCopy).filter(FieldEditorExt.class);
		Map<Composite, List<FieldEditorExt>> editorsPerGroup = StreamSupport.stream(fieldExts.spliterator(), false).collect(Collectors.groupingBy(FieldEditorExt::getParent));
		for (Composite group : editorsPerGroup.keySet()) {
			List<FieldEditorExt> editors = editorsPerGroup.get(group);
			int max = 0;
			if (editors != null) {
				for (FieldEditorExt editor : editors) {
					if (editor instanceof FieldEditor) {
						int numberOfControls = ((FieldEditor) editor).getNumberOfControls();
						if (numberOfControls > max) {
							max = numberOfControls;
						}
					}
				}
			}
			group.setLayout(new GridLayout(Math.max(max - 1, 1), false));
		}
	}

	@Override
	protected void updateFieldEditors(boolean enabled) {
		Composite parent = getFieldEditorParent();
		for (FieldEditor editor : editorsCopy) {
			editor.load();
			if (editor instanceof FieldEditorExt) {
				editor.setEnabled(enabled, ((FieldEditorExt) editor).getParent());
			} else {
				editor.setEnabled(enabled, parent);
			}
		}
		Button defaultsButton = getDefaultsButton();
		if (defaultsButton != null) {
			defaultsButton.setEnabled(enabled);
		}
	}

	protected Composite createSettingsGroup(Composite parent, int styles, String text) {
		Group group = new Group(parent, styles);
		group.setLayout(new GridLayout(1, false)); // We will adjust the columns later.
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(text);
		return group;
	}

	@Override
	protected void createFieldEditors() {
		// General Dialog Settings
		Composite generalSettings = createSettingsGroup(getFieldEditorParent(), SWT.NONE, "Dialog Settings");
		// Graph Settings
		Composite scriptsToSaveSetting = createSettingsGroup(generalSettings, SWT.NONE, "Scripts to Extract");
		addField(new BooleanFieldEditorExt(DialogPreferences.ORIGINAL_LANGUAGE.getId(), "Original language", scriptsToSaveSetting));
		addField(new BooleanFieldEditorExt(DialogPreferences.PYTHON_LANGUAGE.getId(), "Python", scriptsToSaveSetting));
		addField(new BooleanFieldEditorExt(DialogPreferences.OTHER_PYTHON_LANGUAGE.getId(), "Other Python Flavors", scriptsToSaveSetting));
		addField(new BooleanFieldEditorExt(DialogPreferences.VERBOSE_EXTRACTION.getId(), "verboseExtraction", generalSettings));
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), "Text to Triples Host and Port:", generalSettings));
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), "Java to Python Translation Service Host and Port:", generalSettings));
		Composite computationalGraphSettings = createSettingsGroup(generalSettings, SWT.NONE, "Computational Graph Settings");
		addField(new BooleanFieldEditorExt(DialogPreferences.USE_DBN_CG_SERVICE.getId(), "Use DBN", computationalGraphSettings));		
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getId(), "DBN Service Host and Port:", computationalGraphSettings));
		addField(new StringFieldEditorExt(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId(), "DBN Json Generation Service Host and Port:", computationalGraphSettings));
		addField(new BooleanFieldEditorExt(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId(), "Use K-CHAIN", computationalGraphSettings));		
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId(), "K-CHAIN Service Host and Port:", computationalGraphSettings));
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI.getId(), "Invizin Service Host and Port:", computationalGraphSettings));
//		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getId(), "Code Extraction KBase Root:", generalSettings));
		addField(new StringFieldEditorExt(DialogPreferences.SHORT_GRAPH_LINK.getId(), "Short path to Graph folder:", generalSettings));
	}

	@Override
	protected void performDefaults() {
		initializeDefaultPreferences();
		super.performDefaults();
	}

	private void initializeDefaultPreferences() {
		IPreferenceStore store = this.getPreferenceStore();

		//General Settings
		store.setDefault(DialogPreferences.ORIGINAL_LANGUAGE.getId(), DialogPreferences.ORIGINAL_LANGUAGE.getDefaultValue());
		store.setDefault(DialogPreferences.PYTHON_LANGUAGE.getId(), DialogPreferences.PYTHON_LANGUAGE.getDefaultValue());
		store.setDefault(DialogPreferences.OTHER_PYTHON_LANGUAGE.getId(), DialogPreferences.OTHER_PYTHON_LANGUAGE.getDefaultValue());
		store.setDefault(DialogPreferences.VERBOSE_EXTRACTION.getId(), DialogPreferences.VERBOSE_EXTRACTION.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId(), DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId(), DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.USE_DBN_CG_SERVICE.getId(), DialogPreferences.USE_DBN_CG_SERVICE.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getId(), DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getDefaultValue());
		store.setDefault(DialogPreferences.SHORT_GRAPH_LINK.getId(), DialogPreferences.SHORT_GRAPH_LINK.getDefaultValue());
		setPreferenceStore(store);
	}

	@Override
	public boolean performOk() {
		boolean retVal = super.performOk();
		
		if (retVal && isPropertyPage()) {
			// the changes apply only to the current project
			IPreferencesService service = Platform.getPreferencesService();
			String format = service.getString("com.ge.research.sadl.Sadl", "OWL_Format", SadlPersistenceFormat.RDF_XML_ABBREV_FORMAT, null);
		}
		else {
			// the changes apply to all projects
			IPreferencesService service = Platform.getPreferencesService();
			String format = service.getString("com.ge.research.sadl.Sadl", "OWL_Format", SadlPersistenceFormat.RDF_XML_ABBREV_FORMAT, null);
			String dmyOrder = service.getString("com.ge.research.sadl.Sadl", "dmyOrder", "mdy", null);
			String[] itemContent = new String[1];
			itemContent[0] = IConfigurationManager.DateFormat;
			ConfigurationItem ci = new ConfigurationItem(itemContent);
			NameValuePair nvp = ci.new NameValuePair(IConfigurationManager.dmyOrder, dmyOrder);
			ci.addNameValuePair(nvp);
		}
		return retVal;
	}

}
