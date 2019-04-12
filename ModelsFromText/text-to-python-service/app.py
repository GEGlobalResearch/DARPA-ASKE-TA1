"""
********************************************************************** 
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
 ***********************************************************************
"""
from flask import Flask, render_template, request
import json
import math

import pdb


app = Flask(__name__)

#@app.route('/convertTextToCode', methods=['GET','POST'])
@app.route('/convertTextToCode', methods=['GET','POST'])
def convertTextToCode():
	'''
	This is the main entry point. Input is a text string and output is a JSON object
	'''

	inputdata = request.get_json()
	
	### Look for 'equation' in the input JSON object
	pStr = str(inputdata['equation'])
	inpStr = convertToCode(pStr)

	return json.dumps(inpStr)


def isNumber(inputstr):
	'''
	Check if a string is a number
	'''

	try:
		aNum = float(inputstr)
		### Check for NaN ###
		if (math.isnan(aNum)):
			return False
		return True
	except ValueError:
		return False


def getInputArgumentList(inputString1):
	'''
	Extract the input arguments for proper display
	'''

	lhs, rhs = inputString1.split('=')

	### Ignore common operators since they are not input arguments
	for i in rhs:
		if i in ['=','+','-','*','/','(',')']:
			rhs = rhs.replace(i, ' ')
	rhstokens = rhs.split()

	specialTokens = ['tf.math.pow','tf.math.exp']
	mtokens = []
	for i in rhstokens:
		if not isNumber(i) and i not in mtokens and i not in specialTokens:
			mtokens.append(i)

	return mtokens


def modifyEquation(inputStr,inputStrLHS,inputStrRHS,modifExpr):

	'''
	Take an equation string and create a JSON object with the LHS and RHS components, etc.
	'''

	lhs, rhs = modifExpr.split('=')
	modifExpr = modifExpr.replace('"','')
	lhs = lhs.replace('"','')
	rhs = rhs.replace('"','')

	inputArgs = getInputArgumentList(modifExpr)

	lhs = lhs.replace(' ','')
	mitems = lhs.split('**')

	modifObj = dict()
	modifObj['Input String'] = inputStr
	modifObj['inputVars'] = inputArgs
	modifObj['modifiedLHS'] = lhs
	modifObj['modifiedRHS'] = rhs

	specialTokens = ['tf.math.pow','tf.math.exp']

	### Current assumption is that if lhs contains two arguments, it is a case of ^ operation
	if len(mitems) == 2:
		modifObj['codeLHS'] = mitems[0]
		modifObj['codeRHS'] = 'tf.math.pow(' + rhs + ', 1/' + str(mitems[1]) + ')'
		modifObj['codeEquation'] = modifObj['codeLHS'] + ' = ' + modifObj['codeRHS']
		modifObj['TF operations'] = [x for x in specialTokens if x in modifObj['codeEquation']]
		modifObj['returnVar'] = [inputStrLHS]

	jsonObj = json.dumps(modifObj)
	return jsonObj


def convertToCode(inputStr):
	'''
	The main function that converts a text equation to equivalent code
	'''	

	currLine = inputStr.replace(' ','')
	lenCurrLine = len(currLine)

	strLHS, strRHS = currLine.split('=')

	if lenCurrLine > 1:
	    tokens = currLine.split()

	megaExpression = ""
	startEquationFlag = 1
	if startEquationFlag == 1:
		i = 0
		while i < lenCurrLine:
			currChar = currLine[i]
			if i < lenCurrLine - 1:
				nextChar = currLine[i+1]
			else:
				nextChar = ""
			if currChar == '(':
				megaExpression += currChar + " " 
			elif currChar in ['/','+','-']:
				megaExpression += " " + currChar + " " 
			elif currChar == '=':
				leftSide = megaExpression
				megaExpression += " " + currChar + " " 
			elif currChar == ')':
				if nextChar == '(' or nextChar.isalpha():
					megaExpression +=  " " + currChar + " * "
				else:
					megaExpression +=  " " + currChar
			elif currChar == '}' and nextChar == '(':
				megaExpression += currChar + " * " 
			elif currChar == 'e' and nextChar =='^':
				megaExpression +=  " tf.math.exp"
				i = i+1
			elif currChar == '^':
				megaExpression +=  " ** "
				#i = i+1
			elif currChar == '_' and nextChar == '{':
				while currLine[i] != '}':
					megaExpression += currChar
					i = i+1
					currChar = currLine[i]
					nextChar = currLine[i+1]
				megaExpression += '}'
				if i+1 < lenCurrLine:
					if currLine[i+1] == '(' or currLine[i+1] == '{' or (currLine[i+1]).isdigit() or (currLine[i+1]).isalpha():
						megaExpression += " * " 
			elif currChar == '_' and nextChar.isalpha():
				megaExpression +=  currChar + nextChar
				i = i+1
				if i+1 < lenCurrLine:
					if (currLine[i]).isalpha() or (currLine[i]).isdigit():
						if (currLine[i+1]).isalpha() or (currLine[i+1]).isdigit():
							megaExpression += " * "
			elif currChar.isalpha() and nextChar.isdigit():
				megaExpression += currChar + " * " 
			elif currChar.isdigit() and nextChar == '(':
				megaExpression += currChar + " * "
			elif currChar.isalpha() and nextChar == '(':
				megaExpression += currChar + " * "
			elif currChar.isdigit() and nextChar.isalpha():
				megaExpression += currChar + " * "
			elif currChar.isalpha() and nextChar.isalpha():
				currChar = currLine[i]
				nextChar = currLine[i+1]
				while nextChar.isalpha() and i+1 < lenCurrLine:
					currChar = currLine[i]
					nextChar = currLine[i+1]
					megaExpression += currChar
					i = i + 1
				megaExpression += nextChar
			else:
				if currChar == '*':
					megaExpression += " " + currChar + " "
				elif currChar == '[' or currChar == '{':
					megaExpression += " (  "
				elif currChar == ']' or currChar == '}':
					megaExpression += " ) "
				else:
					megaExpression += currChar
			i = i+1
				
		modifObj = modifyEquation(inputStr,strLHS,strRHS,megaExpression)
		#print('\n###################################\nModified Object JSON object =\n', json.dumps(json.loads(modifObj),indent=4))

		return json.loads(modifObj)


@app.route('/')
def home():
    return 'Hello from main container'

if __name__ == "__main__":

    app.run(host="0.0.0.0", port="5002", debug=True)


