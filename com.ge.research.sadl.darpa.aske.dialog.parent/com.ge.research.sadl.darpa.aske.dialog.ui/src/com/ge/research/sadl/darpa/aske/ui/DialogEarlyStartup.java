package com.ge.research.sadl.darpa.aske.ui;

import static com.ge.research.sadl.darpa.aske.dialog.ui.internal.DialogActivator.COM_GE_RESEARCH_SADL_DARPA_ASKE_DIALOG;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import com.ge.research.sadl.darpa.aske.dialog.ui.internal.DialogActivator;

public class DialogEarlyStartup implements IStartup {

	@Override
	public void earlyStartup() {
		CustomDialogHooks hook = DialogActivator.getInstance().getInjector(COM_GE_RESEARCH_SADL_DARPA_ASKE_DIALOG).getInstance(CustomDialogHooks.class);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(hook, IResourceChangeEvent.PRE_BUILD |IResourceChangeEvent.POST_BUILD);
	}

}
