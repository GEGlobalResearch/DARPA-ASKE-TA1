package com.ge.research.sadl.darpa.aske.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.xtext.preferences.PreferenceKey;

@SuppressWarnings("restriction")
public class DialogPreferences {
	public static final PreferenceKey ANSWER_TEXT_SERVICE_BASE_URI = new PreferenceKey("textServiceBaseUri", "http://sadl.org/text");
	public static final PreferenceKey ANSWER_CG_SERVICE_BASE_URI = new PreferenceKey("cgServiceBaseUri", "http://sadl.org/kchain");
	public static final PreferenceKey ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI = new PreferenceKey("java2pythonServiceBaseUri", "http://sadl.org/java2python");
	public static final PreferenceKey ANSWER_CODE_EXTRACTION_KBASE_ROOT = new PreferenceKey("codeExtractionKbaseRott", "resources/CodeModel");
	
	private static final PreferenceKey[] allKeys = {
			ANSWER_TEXT_SERVICE_BASE_URI,
			ANSWER_CG_SERVICE_BASE_URI
	};

	public static final List<PreferenceKey> preferences() {
		return Arrays.asList(allKeys);
	}
}
