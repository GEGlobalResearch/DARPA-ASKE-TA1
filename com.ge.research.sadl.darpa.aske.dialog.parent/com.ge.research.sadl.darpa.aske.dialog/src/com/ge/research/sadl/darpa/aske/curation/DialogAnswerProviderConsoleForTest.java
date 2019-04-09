/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Project: ANSWER, developed with the support of the Defense Advanced 
 * Research Projects Agency (DARPA) under Agreement  No.  HR00111990006. 
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;

public class DialogAnswerProviderConsoleForTest implements IDialogAnswerProvider {

	/* (non-Javadoc)
	 * @see com.ge.research.sadl.darpa.aske.tests.IDialogAnswerProvider#addCurationManagerInitiatedContent(java.lang.String)
	 */
	@Override
	public String addCurationManagerInitiatedContent(String content) {
        Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
        MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
        MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
        initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}
	
	/* (non-Javadoc)
	 * @see com.ge.research.sadl.darpa.aske.tests.IDialogAnswerProvider#initiateMixedInitiativeInteraction(com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement)
	 */
	@Override
	public String initiateMixedInitiativeInteraction(MixedInitiativeElement element) {
		String output = element.getContent().toString();
		System.out.println("CM: " + output + "(" + element.getClass().toString() + ")");
		System.out.println("?");
		read(System.in, 100000, element);
		return "AtTime-" + System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see com.ge.research.sadl.darpa.aske.tests.IDialogAnswerProvider#provideResponse(com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement)
	 */
	@Override
	public void provideResponse(MixedInitiativeElement response) {
		System.out.println("Response: " + response.toString());
	}
	
    /**
     * Non blocking read from input stream using controlled thread
     * 
     * @param is
     *            — InputStream to read
     * @param timeout
     *            — timeout, should not be less that 10
     * @return
     */
	
    // Holder for temporary store of read(InputStream is) value
    private static String threadValue = "";
	private List<Thread> waitingInteractions = new ArrayList<Thread>();

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
	public MixedInitiativeElement getMixedInitiativeElement(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, String methodToCall,
			List<Object> args, String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeMixedInitiativeElement(String key) {
		// TODO Auto-generated method stub
		return false;
	}
}

