package com.ge.research.sadl.darpa.aske.curation;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;

public abstract class BaseDialogAnswerProvider implements IDialogAnswerProvider {

	protected AnswerCurationManager answerConfigurationManager;
	protected Map<String, MixedInitiativeElement> mixedInitiativeElements = new HashMap<>();

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager acm, String content) {
		answerConfigurationManager = acm;
//		Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
		MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
//		MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
//		addMixedInitiativeElement(content, questionElement);
//		initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager acm, String methodToCall, List<Object> args,
			String content) throws TranslationException {

		answerConfigurationManager = acm;
//		Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
		MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
//		MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond, acm, methodToCall, args);
//		addMixedInitiativeElement(content, questionElement);
//		initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}

	@Override
	public String replaceDialogText(AnswerCurationManager acm, EObject eObject, String originalTxt,
			String replacementTxt) {
		answerConfigurationManager = acm;
//		Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
		MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(replacementTxt);
//		MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
//		addMixedInitiativeElement(content, questionElement);
//		initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}

	@Override
	public MixedInitiativeElement getMixedInitiativeElement(String key) {
		return mixedInitiativeElements.get(key);
	}

	protected abstract void displayFiles(String modelFolder, List<File> sadlFiles);

	protected void addMixedInitiativeElement(String key, MixedInitiativeElement element) {
		if (key.endsWith(".") || key.endsWith("?")) {
			// drop EOS
			key = key.substring(0, key.length() - 1);
		}
		mixedInitiativeElements.put(key, element);
	}

	protected IConfigurationManager getConfigMgr() {
		if (answerConfigurationManager != null) {
			return answerConfigurationManager.getConfigurationManager();
		}
		return null;
	}

	@Override
	public boolean removeMixedInitiativeElement(String key) {
		mixedInitiativeElements.remove(key);
		return true;
	}

	@Override
	public void dispose() {
		mixedInitiativeElements.clear();
	}

	public AnswerCurationManager getAnswerConfigurationManager() {
		return answerConfigurationManager;
	}

	public void setAnswerConfigurationManager(AnswerCurationManager answerConfigurationManager) {
		this.answerConfigurationManager = answerConfigurationManager;
	}
}
