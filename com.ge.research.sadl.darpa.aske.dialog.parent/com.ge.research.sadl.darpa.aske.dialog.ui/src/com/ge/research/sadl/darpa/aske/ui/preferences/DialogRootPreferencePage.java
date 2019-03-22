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
import com.ge.research.sadl.reasoner.ConfigurationItem;
import com.ge.research.sadl.reasoner.ConfigurationItem.NameValuePair;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IConfigurationManager;
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
		// General SADL Settings
		Composite generalSettings = createSettingsGroup(getFieldEditorParent(), SWT.NONE, "Dialog Settings");
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), "Text to Triples Service Base URI:", generalSettings));
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI.getId(), "Computational Graph Service Base URI:", generalSettings));
		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), "Java to Python Translation Service Base URI:", generalSettings));
//		addField(new StringFieldEditorExt(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getId(), "Code Extraction KBase Root:", generalSettings));
	}

	@Override
	protected void performDefaults() {
		initializeDefaultPreferences();
		super.performDefaults();
	}

	private void initializeDefaultPreferences() {
		IPreferenceStore store = this.getPreferenceStore();

		//General Settings
		store.setDefault(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_CG_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getDefaultValue());
		store.setDefault(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getId(), DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getDefaultValue());
		setPreferenceStore(store);
	}

	@Override
	public boolean performOk() {
		boolean retVal = super.performOk();
		
		if (retVal && isPropertyPage()) {
			// the changes apply only to the current project
			IPreferencesService service = Platform.getPreferencesService();
			String format = service.getString("com.ge.research.sadl.Sadl", "OWL_Format", ConfigurationManager.RDF_XML_ABBREV_FORMAT, null);
		}
		else {
			// the changes apply to all projects
			IPreferencesService service = Platform.getPreferencesService();
			String format = service.getString("com.ge.research.sadl.Sadl", "OWL_Format", ConfigurationManager.RDF_XML_ABBREV_FORMAT, null);
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
