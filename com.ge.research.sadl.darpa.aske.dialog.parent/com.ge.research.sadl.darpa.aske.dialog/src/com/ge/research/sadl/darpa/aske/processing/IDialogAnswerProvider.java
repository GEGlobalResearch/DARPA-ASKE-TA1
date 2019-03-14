package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;

public interface IDialogAnswerProvider {

	String addCurationManagerInitiatedContent(String content);

	String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, String methodToCall,
			List<Object> args, String content);

	String initiateMixedInitiativeInteraction(MixedInitiativeElement element);

	void provideResponse(MixedInitiativeElement response);

	public MixedInitiativeElement getMixedInitiativeElement(String key);

}