package com.ge.research.sadl.darpa.aske.processing;

import com.ge.research.sadl.processing.SadlConstants;

public class DialogConstants {
	public static final String LAST_DIALOG_COMMAND = "LastDialogCommand";
	public static final String DIALOG_ANSWER_PROVIDER = "DialogAnswerProvider";
	public static final String ANSWER_EXTRACTION_PROCESSOR = "AnswerExtractionProcessor";
	public static final String ANSWER_CURATION_MANAGER = "AnswerCurationManager";
	
	public static final String SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#model";
	public static final String SADL_IMPLICIT_MODEL_DIALOG_DATA_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#data";
	
	public static final String EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT = "ExtractedModels";
	public static final String CODE_EXTRACTION_MODEL_FILENAME = "CodeExtractionModel.sadl";
	public static final String CODE_EXTRACTION_MODEL_URI = "http://sadl.org/CodeExtractionModel.sadl";
	public static final String CODE_EXTRACTION_MODEL_PREFIX = "cem";
	public static final String CODE_EXTRACTION_MODEL_SERIALIZATION_PROPERTY_URI = CODE_EXTRACTION_MODEL_URI + "#serialization";
}
