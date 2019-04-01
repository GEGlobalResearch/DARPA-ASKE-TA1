package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.hp.hpl.jena.ontology.OntModel;

public interface IModelFromCodeExtractor {

//	public class Tag {
//		private String name;
//		private String text;
//		
//		public Tag(String n, String t) {
//			setName(n); 
//			setText(t);
//		}
//
//		public String getName() {
//			return name;
//		}
//
//		public void setName(String name) {
//			this.name = name;
//		}
//
//		public String getText() {
//			return text;
//		}
//
//		public void setText(String text) {
//			this.text = text;
//		}
//	}
//	
	String getPackageName();

	void addCodeFile(File javaFile);

	void addCodeFiles(List<File> javaFiles);
	
	List<File> getCodeFiles();
	
	void setCodeFiles(List<File> codeFiles);

	String getCodeModelFolder();

	void setCodeModelFolder(String codeModelFolder);
	
	IConfigurationManagerForIDE getCodeModelConfigMgr();

	boolean process(String inputIdentifier, String content, boolean includeSerialization) throws ConfigurationException, IOException;

//	Map<String, Tag> getTagMap();
//
	void setDefaultCodeModelName(String defmdlnm);
	
	String getDefaultCodeModelName();
	
	void setDefaultCodeModelPrefix(String prefix);
	
	String getDefaultCodeModelPrefix();

	void setCodeModelPrefix(String codeModelPrefix);

	String getCodeModelPrefix();

	String getCodeModelNamespace();

	void setCodeModelName(String codeModelName);

	void addCodeModel(String key, OntModel codeModel);

	Map<String,OntModel> getCodeModels();

	OntModel getCodeModel(String key);

}