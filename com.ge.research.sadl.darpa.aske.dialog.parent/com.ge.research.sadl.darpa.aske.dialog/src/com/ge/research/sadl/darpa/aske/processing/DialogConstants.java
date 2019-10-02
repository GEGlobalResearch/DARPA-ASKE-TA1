/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.darpa.aske.processing;

import com.ge.research.sadl.processing.SadlConstants;

public class DialogConstants {
	public static final String DIALOG_CONTENTS = "DialogContent";
	public static final String LAST_DIALOG_COMMAND = "LastDialogCommand";
	public static final String DIALOG_ANSWER_PROVIDER = "DialogAnswerProvider";
	public static final String ANSWER_EXTRACTION_PROCESSOR = "AnswerExtractionProcessor";
	public static final String ANSWER_CURATION_MANAGER = "AnswerCurationManager";
	
	public static final String SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#model";
	public static final String SADL_IMPLICIT_MODEL_DIALOG_DATA_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#data";
	public static final String SADL_IMPLICIT_MODEL_QUESTION_ELEMENT_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#QuestionElement";
	public static final String SADL_IMPLICIT_MODEL_ANSWER_ELEMENT_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#AnswerElement";
	public static final String SADL_IMPLICIT_MODEL_TEXT_PROPERY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#text";
	public static final String SADL_IMPLICIT_MODEL_HAS_ANSWER_PROPERY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#dialogAnswer";
	
	public static final String EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT = "ExtractedModels";
	public static final String CODE_EXTRACTION_MODEL_FILENAME = "CodeExtractionModel.sadl";
	public static final String CODE_EXTRACTION_MODEL_URI = "http://sadl.org/CodeExtractionModel.sadl";
	public static final String CODE_EXTRACTION_MODEL_PREFIX = "cem";
	public static final String CODE_EXTRACTION_MODEL_SERIALIZATION_PROPERTY_URI = CODE_EXTRACTION_MODEL_URI + "#serialization";
}
