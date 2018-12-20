package com.ge.research.sadl.darpa.aske.processing;

import com.ge.research.sadl.processing.SadlModelProcessorProvider;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;

@SuppressWarnings("all")
public class DialogModelProcessorProvider extends SadlModelProcessorProvider {
  private static final String EXTENSION_ID = "com.ge.research.sadl.darpa.aske.dialog.dialog_model_processor";
  
  @Inject
  public DialogModelProcessorProvider(final Injector injector) {
    super(injector);
  }
  
  @Override
  protected Optional<String> getExtensionPointId() {
    return Optional.<String>of(DialogModelProcessorProvider.EXTENSION_ID);
  }
}
