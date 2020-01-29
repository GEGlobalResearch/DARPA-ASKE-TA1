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

package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.sADL.SadlModel
import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.junit.Assert.assertNotNull

@RunWith(XtextRunner)
@InjectWith(DialogInjectorProvider)
class DialogParsingTest {

	@Inject
	extension ParseHelper<SadlModel> parseHelper

	@Test
	def void suitable_01() {
		'''
			uri "http://sadl.org/test.dialog".
			Is F100 suitable for MissionX?
		'''.parse.assertNotNull
	}

	@Test
	def void suitable_02() {
		'''
			uri "http://sadl.org/test.dialog".
			Is CF6 suitable when altitude is 25000 ft?
		'''.parse.assertNotNull
	}

	@Test
	def void suitable_03() {
		'''
			uri "http://sadl.org/test.dialog".
			Is a CF6 suitable when altitude is 25000 ft?
		'''.parse.assertNotNull
	}

	@Test
	def void suitable_04() {
		'''
			uri "http://sadl.org/test.dialog".
			Is an CF6 suitable when altitude is 25000 ft?
		'''.parse.assertNotNull
	}

	@Test
	def void suitable_05() {
		'''
			uri "http://sadl.org/test.dialog".
			Is an F100 suitable for (a Mission, requires (an Aircraft with speed 1.0 mach, with altitude 25000 ft, with part
			(an AircraftEngine with thrust 25000 lb, with weight 3500 lb, with sfc 1.5 )))?
		'''.parse.assertNotNull
	}
}
