package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator.SadlMethod;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.github.javaparser.ast.comments.Comment;

public interface IModelFromCodeExtractor {

	public class Tag {
		private String name;
		private String text;
		
		public Tag(String n, String t) {
			setName(n); 
			setText(t);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	
	String getPackageName();

	Set<String> getNames();

	Map<String, String> getClassDeclarations();

	List<SadlMethod> getMethods();

	List<Comment> getComments();

	void addCodeFiles(List<File> javaFiles);
	
	List<File> getCodeFiles();
	
	void setCodeFiles(List<File> codeFiles);

	boolean process(String content, String defaultCodeModelName, String defaultCodeModelPrefix, boolean includeSerialization) throws ConfigurationException, IOException;

	String getTypeComment();

	Map<String, Tag> getTagMap();

	String getType();

}