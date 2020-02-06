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

final class StartTagTypeServerCommonEscaped extends StartTagTypeGenericImplementation {
	static final StartTagTypeServerCommonEscaped INSTANCE=new StartTagTypeServerCommonEscaped();

	private StartTagTypeServerCommonEscaped() {
		super("escaped common server tag","<\\%","%>",null,true);
	}

	protected int getEnd(final Source source, int pos) {
		// Make sure there are no interloping unescaped server tags or server comment tags
		Tag nextServerCommonTag=source.getNextTag(pos,StartTagTypeServerCommon.INSTANCE);
		Tag nextServerCommonCommentTag=source.getNextTag(pos,StartTagTypeServerCommonComment.INSTANCE);
		while (true) {
			int potentialEnd=super.getEnd(source,pos);
			if (potentialEnd==-1) return -1;
			do {
				int skipToPos=pos;
				if (nextServerCommonTag!=null && nextServerCommonTag.getEnd()<=potentialEnd) {
					skipToPos=nextServerCommonTag.getEnd()+1;
				}
				if (nextServerCommonCommentTag!=null && nextServerCommonCommentTag.getEnd()<=potentialEnd) {
					skipToPos=Math.max(skipToPos,nextServerCommonCommentTag.getEnd()+1);
				}
				if (skipToPos==pos) return potentialEnd;
				pos=skipToPos;
				if (nextServerCommonTag!=null && nextServerCommonTag.getEnd()<=pos) nextServerCommonTag=source.getNextTag(pos,StartTagTypeServerCommon.INSTANCE);
				if (nextServerCommonCommentTag!=null && nextServerCommonCommentTag.getEnd()<=pos) nextServerCommonCommentTag=source.getNextTag(pos,StartTagTypeServerCommonComment.INSTANCE);
			} while (pos<potentialEnd);
		}
	}
}
