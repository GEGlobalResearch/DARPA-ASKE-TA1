// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 3.4
// Copyright (C) 2004-2013 Martin Jericho
// http://jericho.htmlparser.net/
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of either one of the following licences:
//
// 1. The Eclipse Public License (EPL) version 1.0,
// included in this distribution in the file licence-epl-1.0.html
// or available at http://www.eclipse.org/legal/epl-v10.html
//
// 2. The GNU Lesser General Public License (LGPL) version 2.1 or later,
// included in this distribution in the file licence-lgpl-2.1.txt
// or available at http://www.gnu.org/licenses/lgpl.txt
//
// 3. The Apache License version 2.0,
// included in this distribution in the file licence-apache-2.0.html
// or available at http://www.apache.org/licenses/LICENSE-2.0.html
//
// This library is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the individual licence texts for more details.

package net.htmlparser.jericho;

import java.util.logging.*;
import java.io.*;

final class LoggerProviderSTDERR implements LoggerProvider {
	public static final LoggerProvider INSTANCE=new LoggerProviderSTDERR();
	
	private static volatile Logger sourceLogger=null;
	
	private LoggerProviderSTDERR() {}

	public Logger getLogger(final String name) {
		return new WriterLogger(new OutputStreamWriter(System.err),name);
	}

	public Logger getSourceLogger() {
		// sourceLogger is declared volatile so can be safely used by multiple threads without synchronization.
		// The worst that can happen is that it is set more than once, but that shouldn't matter.
		if (sourceLogger==null) sourceLogger=getLogger(Source.PACKAGE_NAME);
		return sourceLogger;
	}
}
