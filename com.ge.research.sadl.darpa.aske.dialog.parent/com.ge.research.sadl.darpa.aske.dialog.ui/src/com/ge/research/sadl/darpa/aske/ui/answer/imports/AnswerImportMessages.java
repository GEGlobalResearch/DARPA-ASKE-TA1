/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Red Hat, Inc - WizardProjectsImportPage[_ArchiveSelectTitle,
 * 										   _SelectArchiveDialogTitle]
 * Barry Hathaway - Changes for SADL Imports
 * Andy Crapo - Changes for Dialog Imports
 *******************************************************************************/
package com.ge.research.sadl.darpa.aske.ui.answer.imports;

import org.eclipse.osgi.util.NLS;

public class AnswerImportMessages extends NLS {
	private static final String BUNDLE_NAME = "com.ge.research.sadl.darpa.aske.ui.answer.imports.messages";//$NON-NLS-1$

	// ==============================================================================
	// Data Transfer Wizards
	// ==============================================================================
	public static String DataTransfer_fileSystemTitle;

	public static String DataTransfer_browse;
	public static String DataTransfer_selectTypes;
	public static String DataTransfer_selectAll;
	public static String DataTransfer_deselectAll;
	public static String DataTransfer_refresh;
	public static String DataTransfer_cannotOverwrite;
	public static String DataTransfer_emptyString;
	public static String DataTransfer_scanningMatching;
	public static String DataTransfer_information;

	// --- Import Wizards ---
	public static String DataTransfer_importTitle;

	public static String DataTransfer_importTask;
	public static String ImportOperation_cannotCopy;
	public static String ImportOperation_importProblems;
	public static String ImportOperation_openStreamError;
	public static String ImportOperation_closeStreamError;
	public static String ImportOperation_coreImportError;
	public static String ImportOperation_targetSameAsSourceError;
	public static String ImportPage_filterSelections;

	public static String FileImport_selectSource;
	public static String FileImport_selectSourceTitle;
	public static String FileImport_fromDirectory;
	public static String FileImport_importFileSystem;
	public static String FileImport_overwriteExisting;
	public static String FileImport_createTopLevel;
	public static String FileImport_createVirtualFolders;
	public static String FileImport_importElementsAs;
	public static String FileImport_createVirtualFoldersTooltip;
	public static String FileImport_createLinksInWorkspace;
	public static String FileImport_advanced;
	public static String FileImport_noneSelected;
	public static String FileImport_cannotImportFilesUnderAVirtualFolder;
	public static String FileImport_haveToCreateLinksUnderAVirtualFolder;
	public static String FileImport_invalidSource;
	public static String FileImport_sourceEmpty;
	public static String FileImport_importProblems;
	
	public static String AnswerImport_Page_Title;
	public static String AnswerImport_Page_Description; 
	public static String AnswerImport_title;
	public static String AnswerImport_Encoding_Label;
	public static String AnswerImport_Encoding_Default;
	public static String AnswerImport_Encoding_ASCII;
	public static String AnswerImport_Encoding_ISOLatin;
	public static String AnswerImport_Encoding_UTF8;
	public static String AnswerImport_Encoding_UTF16;
	public static String AnswerImport_Encoding_UTF16LE;
	public static String AnswerImport_Encoding_UTF16BE;
	public static String AnswerImport_Col_Header;
	public static String AnswerImport_Debug;
	public static String AnswerImport_Col_Sep;
	public static String AnswerImport_Row_Header;
	public static String AnswerImport_namespace;
	public static String AnswerImport_imports;
	public static String AnswerImport_imports_add_label;
	public static String AnswerImport_imports_edit_label;
	public static String AnswerImport_imports_remove_label;
	public static String AnswerImport_imports_dialog_shellTitle;
	public static String AnswerImport_imports_dialog_dialogTitle;
	public static String AnswerImport_imports_dialog_message;
	public static String AnswerImport_imports_dialog_label;
	public static String AnswerImport_imports_dialog_error;
	public static String AnswerImport_source_dialog_title;
	public static String AnswerImport_template_title;
	public static String AnswerImport_template_dialog_title;
	public static String AnswerImport_as_owl_label;
	public static String AnswerImport_as_sadl_label;
	public static String AnswerImport_invalid_file_message;
	public static String AnswerImport_csv_field_message;
	public static String AnswerImport_namespace_field_message;
	public static String AnswerImport_invalid_namespace_message;
	public static String AnswerImport_invalid_imports_message;
	public static String AnswerImport_template_field_message;
	public static String AnswerImport_restore_label;
	public static String Answer_Extraction_Output_Filename;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, AnswerImportMessages.class);
	}
}
