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

import java.util.*;

// See OutputSegment.COMPARATOR for javadoc
final class OutputSegmentComparator implements Comparator<OutputSegment> {
	public int compare(final OutputSegment outputSegment1, final OutputSegment outputSegment2) {
		final int begin1=outputSegment1.getBegin();
		final int begin2=outputSegment2.getBegin();
		if (begin1<begin2) return -1;
		if (begin1>begin2) return 1;
		// Segments both start at the same position.
		final int end1=outputSegment1.getEnd();
		final int end2=outputSegment2.getEnd();
		if (end1==end2) return 0; // Both are in the same position and same length, can't distinguish which should come first.
		// If either has zero length it has to come before the other.
		if (begin1==end1) return -1;
		if (begin2==end2) return 1;
		// Neither has zero length. Now we return whichever is longest first.
		return end2-end1;
	}
}

