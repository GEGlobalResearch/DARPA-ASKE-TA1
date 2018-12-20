package com.ge.research.sadl.darpa.aske.processing

import com.ge.research.sadl.processing.SadlModelProcessorProvider
import com.google.common.base.Optional
import com.google.inject.Inject
import com.google.inject.Injector

class DialogModelProcessorProvider extends SadlModelProcessorProvider {

	static val EXTENSION_ID = 'com.ge.research.sadl.darpa.aske.dialog.dialog_model_processor';

	@Inject
	new(Injector injector) {
		super(injector)
	}

	@Override
	override protected getExtensionPointId() {
		return Optional.of(EXTENSION_ID);
	}
	
}