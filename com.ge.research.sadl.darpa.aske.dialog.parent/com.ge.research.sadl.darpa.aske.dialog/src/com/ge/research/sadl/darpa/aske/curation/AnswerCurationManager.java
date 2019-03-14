package com.ge.research.sadl.darpa.aske.curation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;

public class AnswerCurationManager {
	private String domainModelowlModelsFolder;
	
	private IConfigurationManagerForIDE domainModelConfigurationManager;
	private Map<String, String> preferences = null;
	private AnswerExtractionProcessor extractionProcessor = null;

	private IDialogAnswerProvider dialogAnswerProvider = null;	// The instance of an implementer of a DialogAnswerProvider
	public enum SaveAsSadl{SaveAsSadl, DoNotSaveAsSadl, AskUserSaveAsSadl}
	
	public AnswerCurationManager (String modelFolder, IConfigurationManagerForIDE configMgr, Map<String,String> prefs) {
		setDomainModelOwlModelsFolder(modelFolder);
		setDomainModelConfigurationManager(configMgr);
		setPreferences(prefs);
	}

	public String getDomainModelOwlModelsFolder() {
		return domainModelowlModelsFolder;
	}

	private void setDomainModelOwlModelsFolder(String owlModelsFolder) {
		this.domainModelowlModelsFolder = owlModelsFolder;
	}

	public IConfigurationManagerForIDE getDomainModelConfigurationManager() {
		return domainModelConfigurationManager;
	}

	private void setDomainModelConfigurationManager(IConfigurationManagerForIDE projectConfigurationManager) {
		this.domainModelConfigurationManager = projectConfigurationManager;
	}

	private Map<String, String> getPreferences() {
		return preferences;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	public Object getTextProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	public AnswerExtractionProcessor getExtractionProcessor() {
		if (extractionProcessor == null) {
			extractionProcessor = new AnswerExtractionProcessor(this, getPreferences());
		}
		return extractionProcessor;
	}

	public void setExtractionProcessor(AnswerExtractionProcessor extractionProcessor) {
		this.extractionProcessor = extractionProcessor;
	}

	/**
	 * Method to process a set of imports of text and/or code
	 * @param outputFilename 
	 * @throws IOException
	 * @throws ConfigurationException 
	 */
	public void processImports(String outputFilename, SaveAsSadl saveAsSadl) throws IOException, ConfigurationException {
		if (outputFilename.endsWith(".sadl")) {
			outputFilename = outputFilename.substring(0, outputFilename.length() - 5) + ".owl";
		}
		String defPrefix;
		if (outputFilename.endsWith(".owl")) {
			defPrefix = outputFilename.substring(0, outputFilename.length() - 4);
		}
		else {
			defPrefix = outputFilename;
		}
		String defName = "http://com.ge.research.sadl.darpa.aske.answer/" + defPrefix;
		if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelName() == null) {
			getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defName);
		}
		if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelPrefix() == null) {
			getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defPrefix);
		}

		List<File> textFiles = getExtractionProcessor().getTextProcessor().getTextFiles();
		if (textFiles != null) {
			for (File f : textFiles) {
				String content = readFileToString(f);
				getExtractionProcessor().getTextProcessor().process(f.getCanonicalPath(), content, null);
			}
		}
		
		List<File> codeFiles = getExtractionProcessor().getCodeExtractor().getCodeFiles();
		String outputOwlFileName = null;
		if (codeFiles != null) {
			for (File f : codeFiles) {
				String content = readFileToString(f);
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(f.getCanonicalPath());
				getExtractionProcessor().getCodeExtractor().process(fileIdentifier, content, true);				
			}
			File of = new File(new File(getExtractionProcessor().getCodeExtractor().getCodeModelFolder()).getParent() + "/GeneratedModels/" + outputFilename);
			of.getParentFile().mkdirs();
			getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().saveOwlFile(getExtractionProcessor().getCodeModel(), getExtractionProcessor().getCodeModelName(), of.getCanonicalPath());
			outputOwlFileName = of.getCanonicalPath();			
			String altUrl;
			try {
				altUrl = (new SadlUtils()).fileNameToFileUrl(outputOwlFileName);
				getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addMapping(altUrl, getExtractionProcessor().getCodeModelName(), getExtractionProcessor().getCodeModelPrefix(), false, "AnswerCurationManager");
				getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addJenaMapping(getExtractionProcessor().getCodeModelName(), altUrl);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// run inference on the model, interact with user to refine results
		IReasoner reasoner = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().getReasoner();
		String codeModelFolder = getExtractionProcessor().getCodeExtractor().getCodeModelFolder();
		if (reasoner == null) {
			// use domain model folder because that's the project we're working in
			notifyUser(codeModelFolder, "Unable to instantiate reasoner to analyze extracted code model.");
		}
		else {
			if (!reasoner.isInitialized()) {
				IConfigurationManagerForIDE codeModelConfigMgr = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr();
				reasoner.setConfigurationManager(codeModelConfigMgr);
				try {
					reasoner.initializeReasoner(codeModelFolder, getExtractionProcessor().getCodeModelName(), null);
					String queryString = "select ?m where {?m <rdf:type> <Method> . ?m <codemdl:arguments> ?args}";
					queryString = reasoner.prepareQuery(queryString);
					ResultSet results =  reasoner.ask(queryString);
					if (results != null && results.getRowCount() > 0) {
						results.setShowNamespaces(false);
						notifyUser(codeModelFolder, "Interesting methods found in extraction:\n" + results.toStringWithIndent(0, false));
					}
					else {
						notifyUser(codeModelFolder, "No interesting models were found in this extraction from code.");
					}
				} catch (ReasonerNotFoundException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (InvalidNameException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryParseException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryCancelledException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		if (saveAsSadl != null) {
			if (saveAsSadl.equals(SaveAsSadl.AskUserSaveAsSadl)) {
				// ask user if they want a SADL file saved
				IDialogAnswerProvider dap = getDialogAnswerProvider();
				if (dap == null) {
					dap = new DialogAnswerProviderConsoleForTest();
				}
				List<Object> args = new ArrayList<Object>();
				args.add(outputOwlFileName);
// TODO add argument for model folder so we know which project to use in any followup inquiries.				
				dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, "Would you like to save the extracted model in SADL format?");
			}
			if (saveAsSadl != null && saveAsSadl.equals(SaveAsSadl.SaveAsSadl)) {
				saveAsSadlFile(outputOwlFileName);
			}
		}
	}

	/**
	 * Method to save the OWL model created from code extraction as a SADL file
	 * @param outputOwlFileName -- name of the OWL file created
	 * @return -- the sadl fully qualified file name
	 * @throws IOException
	 */
	public String saveAsSadlFile(String outputOwlFileName) throws IOException {
		OwlToSadl ots = new OwlToSadl(getExtractionProcessor().getCodeModel());
		String sadlFN = outputOwlFileName + ".sadl";
		File sf = new File(sadlFN);
		if (sf.exists()) {
			sf.delete();
		}
		try {
			boolean status = ots.saveSadlModel(sadlFN);
			if (status) {
				return sadlFN;
			}
		} catch (OwlImportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void notifyUser(String modelFolder, String msg) throws ConfigurationException {
		if (getDialogAnswerProvider() != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider().getClass().getMethod("addCurationManagerInitiatedContent", String.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic == null) {
				Method[] dapMethods = getDialogAnswerProvider().getClass().getDeclaredMethods();
				if (dapMethods != null) {
					for (Method m : dapMethods) {
						if (m.getName().equals("addCurationManagerInitiatedContent")) {
							acmic = m;
							break;
						}
					}
				}
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					acmic.invoke(getDialogAnswerProvider(), msg);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method to read file content into a string
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String readFileToString(File file) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
//			System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}

	protected IDialogAnswerProvider getDialogAnswerProvider() {
		if (dialogAnswerProvider == null) {
			setDialogAnswerProvider((IDialogAnswerProvider) getDomainModelConfigurationManager().getPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER));
			if (dialogAnswerProvider == null) {
				dialogAnswerProvider = new DialogAnswerProviderConsoleForTest();
			}
		}
		return dialogAnswerProvider;
	}

	protected void setDialogAnswerProvider(IDialogAnswerProvider dialogAnswerProvider) {
		this.dialogAnswerProvider = dialogAnswerProvider;
	}
	
}
