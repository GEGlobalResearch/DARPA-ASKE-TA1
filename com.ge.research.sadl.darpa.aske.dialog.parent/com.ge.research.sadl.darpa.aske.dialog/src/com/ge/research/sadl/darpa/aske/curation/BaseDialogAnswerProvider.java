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

import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.reasoner.utils.SadlUtils;

public abstract class BaseDialogAnswerProvider implements IDialogAnswerProvider {

	protected AnswerCurationManager answerConfigurationManager;
	protected Map<String, MixedInitiativeElement> mixedInitiativeElements = new HashMap<>();

	public String addCurationManagerInitiatedContent(AnswerCurationManager acm, String content) {
		answerConfigurationManager = acm;
		Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
		MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
		MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
		addMixedInitiativeElement(content, questionElement);
		initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager acm, String methodToCall, List<Object> args,
			String content) {

		answerConfigurationManager = acm;
		Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
		MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
		MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond, acm, methodToCall, args);
		addMixedInitiativeElement(content, questionElement);
		initiateMixedInitiativeInteraction(questionElement);
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
			return answerConfigurationManager.getDomainModelConfigurationManager();
		}
		return null;
	}

	@Override
	public void provideResponse(MixedInitiativeElement response) {
		if (response.getCurationManager() != null) {
			AnswerCurationManager acm = response.getCurationManager();
			String methodToCall = response.getMethodToCall();
			Method[] methods = acm.getClass().getMethods();
			for (Method m : methods) {
				if (m.getName().equals(methodToCall)) {
					// call the method
					List<Object> args = response.getArguments();
					try {
						Object results = null;
						if (args.size() == 0) {
							results = m.invoke(acm);
						} else {
							Object arg0 = args.get(0);
							if (args.size() == 1) {
								results = m.invoke(acm, arg0);
							} else {
								Object arg1 = args.get(1);
								if (args.size() == 2) {
									results = m.invoke(acm, arg0, arg1);
								}
								if (methodToCall.equals("saveAsSadlFile")) {
									if (arg0 instanceof List<?> && results instanceof List<?>) {
										String projectName = null;
										List<File> sadlFiles = new ArrayList<File>();
										for (int i = 0; i < ((List<?>) results).size(); i++) {
											Object result = ((List<?>) results).get(i);
											File owlfile = (File) ((List<?>) arg0).get(i);
											if (AnswerCurationManager.isYes(arg1)) {
												File sf = new File(result.toString());
												if (sf.exists()) {
													sadlFiles.add(sf);
													// delete OWL file so it won't be indexed
													if (owlfile.exists()) {
														owlfile.delete();
														try {
															String outputOwlFileName = owlfile.getCanonicalPath();
															String altUrl = new SadlUtils()
																	.fileNameToFileUrl(outputOwlFileName);

															String publicUri = acm.getDomainModelConfigurationManager()
																	.getPublicUriFromActualUrl(altUrl);
															if (publicUri != null) {
																acm.getDomainModelConfigurationManager()
																		.deleteModel(publicUri);
																acm.getDomainModelConfigurationManager()
																		.deleteMapping(altUrl, publicUri);
															}
														} catch (Exception e) {
															e.printStackTrace();
														}
													}
													projectName = sf.getParentFile().getParentFile().getName();
												}
											} else {
												// add import of OWL file from policy file since there won't be a SADL
												// file to build an OWL and create mappings.
												try {
													String importActualUrl = new SadlUtils()
															.fileNameToFileUrl(arg0.toString());
													String altUrl = new SadlUtils().fileNameToFileUrl(importActualUrl);
													String importPublicUri = acm.getDomainModelConfigurationManager()
															.getPublicUriFromActualUrl(altUrl);
													String prefix = acm.getDomainModelConfigurationManager()
															.getGlobalPrefix(importPublicUri);
													acm.getDomainModelConfigurationManager().addMapping(importActualUrl,
															importPublicUri, prefix, false, "AnswerCurationManager");
													String prjname = owlfile.getParentFile().getParentFile().getName();
													IProject prj = ResourcesPlugin.getWorkspace().getRoot()
															.getProject(prjname);
													prj.refreshLocal(IResource.DEPTH_INFINITE, null);
												} catch (Exception e) {
													e.printStackTrace();
													e.printStackTrace();
												}
											}
										}
										if (projectName != null) { // only not null if doing SADL conversion
											IProject project = ResourcesPlugin.getWorkspace().getRoot()
													.getProject(projectName);
											try {
												project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
												// display new SADL file
												displayFiles(getConfigMgr().getModelFolder(), sadlFiles);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
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

}
