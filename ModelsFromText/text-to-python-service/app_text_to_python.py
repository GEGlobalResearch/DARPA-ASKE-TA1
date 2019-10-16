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
import re

import pdb


app = Flask(__name__)

#@app.route('/convertTextToCode', methods=['GET','POST'])
@app.route('/convertTextToCode', methods=['GET','POST'])
def convertTextToCode():
	'''
	This is the main entry point. Input is a text string and output is a JSON object
	'''

	inputdata = request.get_json()
	
	### Look for 'equation' in the input JSON object; should be a text string.
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

	if 'gradients' in inputString1:
		mitems = inputString1.split('tf.gradients')
		trhs = mitems[1]
		matchIndex = trhs.find('stop_gradients')
		rhs = trhs[0:matchIndex]
	else:
		lhs, rhs = inputString1.split('=')

	### Ignore common operators since they are not input arguments
	for i in rhs:
		if i in ['=','+','-','*','/','(',')','[',']',',']:
			rhs = rhs.replace(i, ' ')
	rhstokens = rhs.split()

	specialTokens = ['tf.math.pow','tf.math.exp']
	mtokens = []
	for i in rhstokens:
		if not isNumber(i) and i not in mtokens and i not in specialTokens:
			mtokens.append(i)

	return mtokens


def modifyEquation(inputStr, inputStrLHS, inputStrRHS, modifExpr, gradients):

	'''
	Take an equation string and create a JSON object with the LHS and RHS components, etc.
	The return value is this JSON object
	'''

	if gradients == 1:
		
		### For equations containing tf.gradient, there are essentially two equations, joined by a newline
		if '\n' in modifExpr:
			eqn1, eqn2 = modifExpr.split('\n')
			inputArgs1 = []
			inputArgs2 = []
			lhs1 = ""
			rhs1 = ""
			lhs2 = ""
			rhs2 = ""
			if 'gradients' in eqn1:
				eqn1 = eqn1.replace('"','')
				firstEqualSign = eqn1.find('=')
				inputArgs1 = getInputArgumentList(eqn1)
				lhs1 = eqn1[:firstEqualSign]
				rhs1 = eqn1[firstEqualSign+1:]
			if 'gradients' not in eqn2:
				lhs2, rhs2 = eqn2.split('=')
				eqn2 = eqn2.replace('"','')
				inputArgs2 = getInputArgumentList(eqn2)
			inputArgs = inputArgs1 + inputArgs2
			lhs = lhs1 + '\n' + '    ' + lhs2
			rhs = rhs1 + '\n' + '    ' + rhs2
		else:
			lhs, rhs = modifExpr.split('=')
			modifExpr = modifExpr.replace('"','')
			inputArgs = getInputArgumentList(modifExpr)
	else:
		lhs, rhs = modifExpr.split('=')
		modifExpr = modifExpr.replace('"','')
		inputArgs = getInputArgumentList(modifExpr)

	lhs = lhs.replace('"','').strip()
	rhs = rhs.replace('"','').strip()

	lhs = lhs.replace(' ','')
	mitems = lhs.split('**')

	### Create the JSON object which will be returned in the end
	### It has three top-level variables. The 'text' and 'code
	### variables are the ones that are important. The 'extras'
	### variable is more for use in case of debugging
	modifObj = dict()
	modifObj['text'] = dict()
	modifObj['code'] = dict()
	modifObj['extras'] = dict()

	modifObj['text']['inputString'] = inputStr
	modifObj['text']['inputVars'] = inputArgs
	modifObj['code']['inputVars'] = inputArgs

	modifObj['extras']['modifiedLHS'] = lhs
	modifObj['extras']['modifiedRHS'] = rhs
	modifObj['extras']['codeEquation'] = lhs + ' = ' + rhs

	if gradients == 1:
		lhs1, lhs2 = lhs.split('\n')
		rhs1, rhs2 = rhs.split('\n')
		modifObj['extras']['codeEquation'] = lhs1.strip(' ') + '=' + rhs1.strip(' ') + '\n    ' + lhs2.strip(' ') + '=' + rhs2.strip(' ')
	modifObj['code']['outputVars'] = list(str(lhs).split())
	modifObj['text']['outputVars'] = list(str(inputStrLHS).split())

	if gradients == 1:
		modifObj['text']['type'] = 'gradient'
		modifObj['code']['type'] = 'gradient'
	else:
		modifObj['text']['type'] = 'no_gradients'
		modifObj['code']['type'] = 'no_gradients'

	specialTokens = ['tf.math.pow','tf.math.exp']

	### Current assumption is that if lhs contains two arguments, it is a case of ^ operation
	### This is the special case of a^2 on the LHS.
	if len(mitems) == 2:
		modifObj['code']['outputVars'] = mitems[0]

		modifObj['extras']['codeLHS'] = mitems[0]
		modifObj['extras']['codeRHS'] = 'tf.math.pow(' + rhs + ', 1/' + str(mitems[1]) + ')'
		modifObj['extras']['codeEquation'] = modifObj['extras']['codeLHS'] + ' = ' + modifObj['extras']['codeRHS']
		modifObj['code']['tfCode'] = modifObj['extras']['codeLHS'] + ' = ' + modifObj['extras']['codeRHS']
		modifObj['code']['pyCode'] = modifObj['extras']['codeLHS'] + ' = ' + modifObj['extras']['codeRHS']

		modifObj['extras']['TF operations'] = [x for x in specialTokens if x in modifObj['extras']['codeEquation']]

		modifObj['text']['type'] += ',lhs_has_power_(^)_operation'
		modifObj['code']['type'] += ',lhs_has_power_(^)_operation'

	modifObj['code']['tfCode'] = modifObj['extras']['codeEquation']
	modifObj['code']['pyCode'] = modifObj['extras']['codeEquation']

	if 'tf.' in modifObj['extras']['codeEquation']:
		modifObj['code']['pyCode'] = modifObj['code']['tfCode'].replace('tf.','')


	jsonObj = json.dumps(modifObj)
	return jsonObj


def extractExpression(inputStr, gradientFlag):
	'''
	Convert the given inputStr which is in text format, convert it into a code string
	'''

	currLine = inputStr.replace(' ','')
	lenCurrLine = len(currLine)

	if gradientFlag == 1:
		strLHS = ""
		strRHS = ""
	else:
		strLHS, strRHS = currLine.split('=')

	megaExpression = ""
	startEquationFlag = 1
	lhsProcessedFlag = 0
	specialExpCheckFlag = 0
	if startEquationFlag == 1:
		i = 0
		while i < lenCurrLine:
			currChar = currLine[i]
			if i < lenCurrLine - 1:
				nextChar = currLine[i+1]
			else:
				nextChar = ""

			if gradientFlag == 1 and currChar == '/':
				''' just checking '''
			elif currChar == '(':
				megaExpression += currChar + " " 
			elif currChar in ['/','+','-']:
				megaExpression += " " + currChar + " " 
			elif currChar == '=':
				leftSide = megaExpression
				lhsProcessedFlag = 1
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
				tfPowExpr = " tf.math.pow( "

				k = i-1
				backCount = 1
				subStr = ""
				if currLine[k] == ')':
					k = k - 1
					while (currLine[k] != '('):
						subStr += currLine[k]
						k = k - 1
						backCount += 1

					subStr2 = subStr[::-1]

					##### special check - backwards for tf.math.exp #####
					if currLine[k-1] == '^' and currLine[k-2] == 'e':

						lastOccurExp = megaExpression.rfind('tf.math.exp')
						part1 = megaExpression[:lastOccurExp]
						part1 = part1.strip()[:-1]
						part2 = megaExpression[lastOccurExp:]
						part2 = part2[:-2]
						tmpStr = part1 + tfPowExpr + part2 + ','
						megaExpression = tmpStr
						specialExpCheckFlag = 1

					else:
						
						p = len(megaExpression) - 1

						count=0
						while megaExpression[p] != '(':
							p = p - 1
							count += 1
						p = p - 2
						newExpr = megaExpression[0:p]
						if newExpr[-1] != '(':
							tmpNewExpr = newExpr + '('
							newExpr = tmpNewExpr
						megaExpression = newExpr + tfPowExpr + subStr2 + ','
						specialExpCheckFlag = 1
				else:
					
					if lhsProcessedFlag == 0:
						megaExpression +=  " ** "
					

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
				currChar = currLine[i]
				if i+1 < lenCurrLine:
					currChar = currLine[i]
					nextChar = currLine[i+1]
					if gradientFlag == 1 and currChar == '/':
						if currLine[i+2].isalpha() and not currLine[i+3].isalpha():
							denominator = currLine[i+2]
							numerator = currLine[i-1]
							gradPart = 'g_' + numerator + '_' + denominator
							mlen = len(megaExpression)
							tmpExpr = megaExpression[:mlen-2]
							megaExpression = tmpExpr + gradPart

							gradExpr = gradPart + ' = tf.gradients(' + numerator + ', [' + denominator +\
										 '], stop_gradients=[' + denominator + '], name=\'' + gradPart + '\')'
							tmpExpr = gradExpr + "\n    " + megaExpression
							megaExpression = tmpExpr

							i = i + 3
							currChar = currLine[i]
				megaExpression += currChar
			else:
				if currChar == '*':
					if specialExpCheckFlag == 1:
						megaExpression += ") " + currChar + " "
						specialExpCheckFlag = 0
					else:
						megaExpression += " " + currChar + " "
				elif currChar == '[' or currChar == '{':
					megaExpression += " (  "
				elif currChar == ']' or currChar == '}':
					if currChar == ']' and specialExpCheckFlag == 1:
						megaExpression += " )) "
					else:
						megaExpression += " ) "
					
				else:
					megaExpression += currChar
			i = i+1
				
	return megaExpression, strLHS, strRHS

def convertToCode(inputStr):
	'''
	The main function that converts a text equation to equivalent code
	'''	

	currLine = inputStr.replace(' ','')

	gradientFlag = 0
	pattern = 'd\S+\/d\S+'
	result = re.search(pattern, currLine)
	if result:
		gradientFlag = 1
		
		gradPattern = 'd\(.*\)\/d'
		result2 = re.search(gradPattern, currLine)

		if result2:
			endChar = result2.end()
			matchedStr = result2.group(0)
			matchedStr = matchedStr.strip()
			if matchedStr[0] == 'd' and matchedStr[1] == '(':
				tmpStr = matchedStr[2:]
			s1,s2 = tmpStr.split(')')
			subExpression = s1
			(megaExpression, strLHS, strRHS) = extractExpression(subExpression, gradientFlag)
			if strLHS == '':
				strLHS, strRHS = inputStr.split('=')
			denominator = currLine[endChar]

			numerator = 'X'
			gradPart = 'g_' + numerator + '_' + denominator
			gradExpr = gradPart + ' = tf.gradients(' + numerator + ', [' + denominator +\
							 '], stop_gradients=[' + denominator + '], name=\'' + gradPart + '\')'
			tmpExpr = gradExpr + "\n    " + numerator + ' = ' + megaExpression
			megaExpression = tmpExpr
		else:
			(megaExpression, strLHS, strRHS) = extractExpression(inputStr, gradientFlag)
			if strLHS == '':
				strLHS, strRHS = inputStr.split('=')

	else:
		gradientFlag = 0
		(megaExpression, strLHS, strRHS) = extractExpression(inputStr, gradientFlag)

	modifObj = modifyEquation(inputStr,strLHS,strRHS,megaExpression,gradientFlag)
	#print('\n###################################\nModified Object JSON object =\n', json.dumps(json.loads(modifObj),indent=4))

	return json.loads(modifObj)


@app.route('/')
def home():
    return 'Hello from main container'

if __name__ == "__main__":

    app.run(host="0.0.0.0", port="5002", debug=True)


