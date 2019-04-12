/**
 * 
 */
package com.ge.research.sadl.darpa.aske.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.preferences.PreferenceKey;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer;

import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.preferences.SadlPreferences;

/**
 * @author dhuebner
 *
 */
@SuppressWarnings("restriction")
public class DialogPreferencesInitializer implements IPreferenceStoreInitializer {
	
	@Override
	public void initialize(IPreferenceStoreAccess access) {
		IPreferenceStore store = access.getWritablePreferenceStore();
		for (PreferenceKey prefKey : DialogPreferences.preferences()) {
			store.setDefault(prefKey.getId(), prefKey.getDefaultValue());
		}
	}


}
