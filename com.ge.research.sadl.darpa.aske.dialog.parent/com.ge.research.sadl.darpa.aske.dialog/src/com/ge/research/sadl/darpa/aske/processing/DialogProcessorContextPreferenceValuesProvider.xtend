package com.ge.research.sadl.darpa.aske.processing

import com.ge.research.sadl.preferences.SadlPreferences
import com.ge.research.sadl.processing.IModelProcessor.ProcessorContextPreferenceValuesProvider
import java.util.List
import org.eclipse.emf.common.EMFPlugin
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.preferences.IPreferenceValues
import org.eclipse.xtext.preferences.MapBasedPreferenceValues
import org.eclipse.xtext.preferences.PreferenceKey
import org.eclipse.xtext.preferences.PreferenceValuesByLanguage
import org.eclipse.xtext.resource.XtextResource

class DialogProcessorContextPreferenceValuesProvider extends ProcessorContextPreferenceValuesProvider {
	
	static val SADL_LANGUAGE_ID = 'com.ge.research.sadl.SADL'

	override getPreferenceValues(Resource resource) {
		// https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/issues/24
		// This is workaround.
		// We want to use the SADL preferences in the SADL model processor although we have a `.dialog` resource.
		val IPreferenceValues sadlPreferenceValues = if (EMFPlugin.IS_ECLIPSE_RUNNING) {
			val dummySadlResource = new XtextResource(URI.createFileURI('dummy.sadl'))
			preferenceProvider.getPreferenceValues(dummySadlResource)
		} else {
			val existingPreferenceValues = PreferenceValuesByLanguage.findInEmfObject(resource.resourceSet)
			if (existingPreferenceValues !== null && existingPreferenceValues.get(SADL_LANGUAGE_ID) !== null) {
				existingPreferenceValues.get(SADL_LANGUAGE_ID)
			} else {
				val sadlPreferences = SadlPreferences.preferences.toMap([id], [defaultValue]);
				val preferenceValues = new MapBasedPreferenceValues(sadlPreferences);
				val referenceValuesByLanguage = new PreferenceValuesByLanguage;
				referenceValuesByLanguage.put(SADL_LANGUAGE_ID, preferenceValues);
				referenceValuesByLanguage.attachToEmfObject(resource.resourceSet);
				preferenceValues
			}
		}
		if (resource.URI.toString.endsWith('.dialog')) {
			if (sadlPreferenceValues !== null) {
				val dialogPreferenceValues = preferenceProvider.getPreferenceValues(resource)
				return new CompositePreferenceValues(#[sadlPreferenceValues, dialogPreferenceValues])
			}
		}
		return super.getPreferenceValues(resource)
	}
	
	/**
	 * The order of the preference values does matter!
	 */
	@FinalFieldsConstructor
	static class CompositePreferenceValues implements IPreferenceValues {
		
		val List<IPreferenceValues> preferenceValues
		
		override getPreference(PreferenceKey key) {
			val defaultValue = key.defaultValue
			// Find a value which is not the default one, so we know we have a hit.
			for (preferenceValue : preferenceValues) {
				val value = preferenceValue.getPreference(key)
				if (value != defaultValue) {
					return value
				}
			}
			// Otherwise, just return with the default value as we did not have a hit.
			return defaultValue
		}
		
	}

}
