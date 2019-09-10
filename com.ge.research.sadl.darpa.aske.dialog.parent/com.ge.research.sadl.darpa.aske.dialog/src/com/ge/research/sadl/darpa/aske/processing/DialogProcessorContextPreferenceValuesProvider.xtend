package com.ge.research.sadl.darpa.aske.processing

import com.ge.research.sadl.processing.IModelProcessor.ProcessorContextPreferenceValuesProvider
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.preferences.IPreferenceValues
import org.eclipse.xtext.preferences.PreferenceKey
import org.eclipse.xtext.resource.XtextResource

class DialogProcessorContextPreferenceValuesProvider extends ProcessorContextPreferenceValuesProvider {

	override getPreferenceValues(Resource resource) {
		if (resource.URI.toString.endsWith('.dialog')) {
			// https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/issues/24
			// This is workaround.
			// We want to use the SADL preferences in the SADL model processor although we have a `.dialog` resource.
			val dummySadlResource = new XtextResource(URI.createFileURI('dummy.sadl'))
			val sadlPreferenceValues = preferenceProvider.getPreferenceValues(dummySadlResource)
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
