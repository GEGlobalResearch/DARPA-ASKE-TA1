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

final class LoggerProviderJCL implements LoggerProvider {
	public static final LoggerProvider INSTANCE=new LoggerProviderJCL();

	private static volatile Logger sourceLogger=null;
	
	private LoggerProviderJCL() {}

	public Logger getLogger(final String name) {
		return new JCLLogger(org.apache.commons.logging.LogFactory.getLog(name));
	}

	public Logger getSourceLogger() {
		// sourceLogger is declared volatile so can be safely used by multiple threads without synchronization.
		// The worst that can happen is that it is set more than once, but loggers of the same name are typically identical in functionality so that shouldn't matter.
		if (sourceLogger==null) sourceLogger=getLogger(Source.PACKAGE_NAME);
		return sourceLogger;
	}
	
	private static class JCLLogger implements Logger {
		private final org.apache.commons.logging.Log jclLog;
		
		public JCLLogger(final org.apache.commons.logging.Log jclLog) {
			this.jclLog=jclLog;
		}

		public void error(final String message) {
			jclLog.error(message);
		}
	
		public void warn(final String message) {
			jclLog.warn(message);
		}
	
		public void info(final String message) {
			jclLog.info(message);
		}
	
		public void debug(final String message) {
			jclLog.debug(message);
		}
	
		public boolean isErrorEnabled() {
			return jclLog.isErrorEnabled();
		}
	
		public boolean isWarnEnabled() {
			return jclLog.isWarnEnabled();
		}
	
		public boolean isInfoEnabled() {
			return jclLog.isInfoEnabled();
		}
	
		public boolean isDebugEnabled() {
			return jclLog.isDebugEnabled();
		}
	}
}
