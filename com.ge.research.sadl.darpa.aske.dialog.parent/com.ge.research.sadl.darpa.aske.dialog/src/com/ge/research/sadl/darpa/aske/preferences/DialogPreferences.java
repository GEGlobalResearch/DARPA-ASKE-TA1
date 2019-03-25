package com.ge.research.sadl.darpa.aske.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.xtext.preferences.PreferenceKey;

@SuppressWarnings("restriction")
public class DialogPreferences {
	public static final PreferenceKey ANSWER_TEXT_SERVICE_BASE_URI = new PreferenceKey("textServiceBaseUri", "http://localhost:4200");
	public static final PreferenceKey ANSWER_CG_SERVICE_BASE_URI = new PreferenceKey("cgServiceBaseUri", "http://localhost:");
	public static final PreferenceKey ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI = new PreferenceKey("java2pythonServiceBaseUri", "http://localhost:19092");
	public static final PreferenceKey ANSWER_CODE_EXTRACTION_KBASE_ROOT = new PreferenceKey("codeExtractionKbaseRoot", "resources/CodeModel");
	
	private static final PreferenceKey[] allKeys = {
			ANSWER_TEXT_SERVICE_BASE_URI,
			ANSWER_CG_SERVICE_BASE_URI
	};

	public static final List<PreferenceKey> preferences() {
		return Arrays.asList(allKeys);
	}
}
