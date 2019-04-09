/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.darpa.aske.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;

public class MixedInitiativeElement {
	private String key = null;
	private Object content = null;
	private MixedInitiativeElement next = null;
	private MixedInitiativeElement previous = null;
	private Consumer<MixedInitiativeElement> respondTo;
	private AnswerCurationManager curationManager = null;
	private String methodToCall = null;
	private List<Object> arguments = null;
	
	public MixedInitiativeElement(Object content, Consumer<MixedInitiativeElement> respondTo) {
		setRespondTo(respondTo);
		setContent(content);
		setKey("Key" + System.currentTimeMillis());
	}

	public MixedInitiativeElement(Object content, Consumer<MixedInitiativeElement> respondTo, AnswerCurationManager acm, String meth, List<Object> args) {
		this(content, respondTo);
		setCurationManager(acm);
		setMethodToCall(meth);
		setArguments(args);
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Object getContent() {
		return content;
	}
	
	public void setContent(Object content) {
		this.content = content;
	}
	
	public MixedInitiativeElement getNext() {
		return next;
	}
	
	public void setNext(MixedInitiativeElement next) {
		this.next = next;
	}
	
	public MixedInitiativeElement getPrevious() {
		return previous;
	}
	
	public void setPrevious(MixedInitiativeElement previous) {
		this.previous = previous;
	}

	public Consumer<MixedInitiativeElement> getRespondTo() {
		return respondTo;
	}

	public void setRespondTo(Consumer<MixedInitiativeElement> respondTo2) {
		this.respondTo = respondTo2;
	}
	
	public String toString() {
		return getContent() != null ? getContent().toString() : "<null> content";
	}

	public AnswerCurationManager getCurationManager() {
		return curationManager;
	}

	public void setCurationManager(AnswerCurationManager curationManager) {
		this.curationManager = curationManager;
	}

	public String getMethodToCall() {
		return methodToCall;
	}

	public void setMethodToCall(String methodToCall) {
		this.methodToCall = methodToCall;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}

	public void addArgument(String arg) {
		if (arguments == null) {
			arguments = new ArrayList<Object>();
		}
		arguments.add(arg);
	}
}
