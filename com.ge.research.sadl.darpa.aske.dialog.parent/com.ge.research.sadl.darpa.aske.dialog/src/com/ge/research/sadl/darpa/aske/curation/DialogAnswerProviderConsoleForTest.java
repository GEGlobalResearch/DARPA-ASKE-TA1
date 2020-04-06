/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************/
package com.ge.research.sadl.darpa.aske.curation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.processing.AnswerContent;
import com.ge.research.sadl.darpa.aske.processing.ExpectsAnswerContent;
import com.ge.research.sadl.darpa.aske.processing.InformationContent;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;
import com.ge.research.sadl.darpa.aske.processing.QuestionWithCallbackContent;
import com.ge.research.sadl.darpa.aske.processing.StatementContent;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.hp.hpl.jena.ontology.OntModel;

public class DialogAnswerProviderConsoleForTest extends BaseDialogAnswerProvider {

	private static String threadValue = "";
	private Scanner userInputScanner = null;
	private List<Thread> waitingInteractions = new ArrayList<Thread>();
	
	public DialogAnswerProviderConsoleForTest() {
		super();
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, String content) {
		setAnswerConfigurationManager(answerCurationManager);
		InformationContent ic = new InformationContent(null, Agent.CM, content);
        initiateMixedInitiativeInteraction(ic);
		return "success";
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, StatementContent ssc) {
		addCurationManagerAnswerContent(getAnswerConfigurationManager(), ssc.getText(), ssc.getHostEObject());
		return "success";
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, String methodToCall,
			List<Object> args, String content) {
		setAnswerConfigurationManager(answerCurationManager);
//        Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
        MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
//        MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond, answerCurationManager, methodToCall, args);
//		addMixedInitiativeElement(content, questionElement);
//		getCurationManager().addToConversation(new ConversationElement(getCurationManager().getConversation(), questionElement, Agent.CM));
//        initiateMixedInitiativeInteraction(questionElement);
		QuestionWithCallbackContent qwcc = new QuestionWithCallbackContent(null, Agent.CM, methodToCall, args, content);
        initiateMixedInitiativeInteraction(qwcc);
        answerCurationManager.addUnansweredQuestion(content, qwcc);
		return "success";
	}

	@Override
	public boolean addCurationManagerAnswerContent(AnswerCurationManager acm, String content, Object ctx) {
		System.out.println(content);
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void dispose() {
		super.dispose();
		for (Thread thread : waitingInteractions) {
			thread.stop();
		}
		waitingInteractions.clear();
		threadValue = "";
	}

	@Override
	public Resource getResource() {
		return null;
	}

	@Override
	protected void displayFiles(String modelFolder, List<File> sadlFiles) {
		System.out.println("Displaying files:");
		for (File f : sadlFiles) {
			try {
				System.out.println("   " + f.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	protected IConfigurationManager getConfigMgr() {
		if (answerConfigurationManager != null) {
			return answerConfigurationManager.getConfigurationManager();
		}
		return null;
	}

	private String getUserInput() {
		String answer = null;
		if (userInputScanner == null) {
			userInputScanner = new Scanner(System.in);
		} else {
			userInputScanner.reset();
		}
		while (answer == null) {
			try {
				answer = userInputScanner.next();
				if (answer.startsWith("Save ") || answer.startsWith("save ")) {
					// this is not a yes/no answer so don't put it into the conversation but
					// simulate the model processor process it
					return null;
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
//		System.out.println("The answer you entered is : " + answer);
//		return "AtTime-" + System.currentTimeMillis();
//		userInputScanner.close();
//		getCurationManager().addToConversation(new ConversationElement(getCurationManager().getConversation(), answer, Agent.USER));
		return answer;
	}

	// Holder for temporary store of read(InputStream is) value
	/**
	 * Non blocking read from input stream using controlled thread
	 * 
	 * @param is      — InputStream to read
	 * @param timeout — timeout, should not be less that 10
	 * @return
	 */
	String read(final InputStream is, int timeout, MixedInitiativeElement element) {
		// Start reading bytes from stream in separate thread
		Thread thread = new Thread() {
			public void run() {
				byte[] buffer = new byte[1024]; // read buffer
				byte[] readBytes = new byte[0]; // holder of actually read bytes
				try {
					Thread.sleep(5);
					// Read available bytes from stream
					int size = is.read(buffer);
					if (size > 0)
						readBytes = Arrays.copyOf(buffer, size);
					// and save read value in static variable
					setValue(new String(readBytes, "UTF-8"));
					String val = getValue();
					System.out.println(val);

					// construct response
					MixedInitiativeElement response = new MixedInitiativeElement(val, null);
					response.setContent(new MixedInitiativeTextualResponse(val));
					// make call identified in element
					element.getRespondTo().accept(response);
				} catch (Exception e) {
					System.err.println("Error reading input stream\nStack trace:\n" + e.getStackTrace());
				}
			}
		};
		thread.start(); // Start thread
		addThreadToWaitingInteractions(thread);
//        try {
//            thread.join(timeout); // and join it with specified timeout
//        } catch (InterruptedException e) {
//            System.err.println("Data was not read in " + timeout + " ms");
//        }
//        return getValue();
		return null;
	}

	private void addThreadToWaitingInteractions(Thread thread) {
		waitingInteractions.add(thread);
	}

	private synchronized void setValue(String value) {
		threadValue = value;
	}

	private synchronized String getValue() {
		String tmp = new String(threadValue);
		setValue("");
		return tmp;
	}

	@Override
	public String initiateMixedInitiativeInteraction(QuestionWithCallbackContent element) {
		String output = element.getTheQuestion();
		System.out.println("CM: " + output);
		String answer = null;
		if (output.trim().endsWith("?")) {
//			System.out.println("?");
//			read(System.in, 100000, element);
			answer = getUserInput();
			element.getArguments().add(answer);
			AnswerContent ac = new AnswerContent(null, Agent.USER);
			ac.setAnswer(answer);
			element.setAnswer(ac);
			provideResponse(element);
		}
		return answer;
	}

	private void initiateMixedInitiativeInteraction(InformationContent ic) {
		addCurationManagerAnswerContent(getAnswerConfigurationManager(), ic.getInfoMessage(), ic.getHostEObject());
	}

	/* (non-Javadoc)
	 * @see com.ge.research.sadl.darpa.aske.tests.IDialogAnswerProvider#provideResponse(com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement)
	 */
	@Override
	public void provideResponse(QuestionWithCallbackContent question) {
//		System.out.println("Response: " + response.toString());
		StatementContent answer = question.getAnswer();
//		if (response.getCurationManager() != null) {
//			setCurationManager(response.getCurationManager());
			String methodToCall = question.getMethodToCall();
			Method[] methods = getAnswerConfigurationManager().getClass().getMethods();
			for (Method m : methods) {
				if (m.getName().equals(methodToCall)) {
					// call the method
					List<Object> args = question.getArguments();
					try {
						Object results = null;
						if (args.size() == 0) {
							results = m.invoke(getAnswerConfigurationManager(), null);
						}
						else {
							Object arg0 = args.get(0);
							if (args.size() == 1) {
								results = m.invoke(getAnswerConfigurationManager(), arg0);
							}
							else {
								Object arg1 = args.get(1);
								if (args.size() == 2) {
									results = m.invoke(getAnswerConfigurationManager(), arg0, arg1);
								}
								if (methodToCall.equals("saveAsSadlFile")) {
									if (arg0 instanceof Map<?,?> && results instanceof List<?>) {
										String projectName = null;
										List<File> sadlFiles = new ArrayList<File>();
										for (int i = 0; i < ((List<?>) results).size(); i++) {
											Object result = ((List<?>) results).get(i);
											File owlfile = (File) ((Map<?,?>) arg0).keySet().iterator().next();
											if (AnswerCurationManager.isYes(arg1)) {
												File sf = new File(result.toString());
												if (sf.exists()) {
													sadlFiles.add(sf);
													// delete OWL file so it won't be indexed
													if (owlfile.exists()) {
														owlfile.delete();
														try {
															String outputOwlFileName = owlfile.getCanonicalPath();
															String altUrl = new SadlUtils().fileNameToFileUrl(outputOwlFileName);
															
															String publicUri = getAnswerConfigurationManager().getConfigurationManager().getPublicUriFromActualUrl(altUrl);
															if (publicUri != null) {
																getAnswerConfigurationManager().getConfigurationManager().deleteModel(publicUri);
																getAnswerConfigurationManager().getConfigurationManager().deleteMapping(altUrl, publicUri);
															}
														} catch (ConfigurationException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														} catch (URISyntaxException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
													projectName = sf.getParentFile().getParentFile().getName();
												}
											}				
											else {
												// add import of OWL file from policy file since there won't be a SADL file to build an OWL and create mappings.
												try {
													String importActualUrl = new SadlUtils().fileNameToFileUrl(arg0.toString());
													String altUrl = new SadlUtils().fileNameToFileUrl(importActualUrl);
													String importPublicUri = getAnswerConfigurationManager().getConfigurationManager().getPublicUriFromActualUrl(altUrl);
													String prefix = getAnswerConfigurationManager().getConfigurationManager().getGlobalPrefix(importPublicUri);
													getAnswerConfigurationManager().getConfigurationManager().addMapping(importActualUrl, importPublicUri, prefix, false, "AnswerCurationManager");
													String prjname = owlfile.getParentFile().getParentFile().getName();
													IProject prj = ResourcesPlugin.getPlugin().getWorkspace().getRoot().getProject(prjname);
													prj.refreshLocal(IResource.DEPTH_INFINITE, null);
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (URISyntaxException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (ConfigurationException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (CoreException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}
										if (answer instanceof AnswerContent) {
											((AnswerContent) answer).setOtherResults(sadlFiles);
										}
										if (projectName != null) {	// only not null if doing SADL conversion
											try {
												IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
												project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
												// display new SADL file
											} catch (IllegalStateException e) {
												// OK, not really in Eclipse
											} catch (CoreException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											try {
												displayFiles(getConfigMgr().getModelFolder(), sadlFiles);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}
							}
						}
//						System.out.println(result.toString());
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
					break;
				}
			}
//		}	
	}

	@Override
	public void updateProjectAndDisplaySadlFiles(String projectName, String modelFolder, List<File> sadlFiles) {
		displayFiles(modelFolder, sadlFiles);
		
	}

	@Override
	public boolean addImports(List<String> importStatements) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String processUserQueryNewThreadWithBusyIndicator(Resource resource, OntModel theModel, String modelName,
			ExpectsAnswerContent sc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addUserContentToDialog(AnswerCurationManager acm, String content, boolean quote) {
		// TODO Auto-generated method stub
		return false;
	}

}