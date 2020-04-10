"""
********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2020 - General Electric Company, All Rights Reserved
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
from pdb import set_trace as bp


app = Flask(__name__)

@app.route('/convertTextToCode', methods=['GET','POST'])
def convertTextToCode():
    '''
    This is the main entry point. Input is a text string and output is a JSON object
    '''

    inputdata = request.get_json()
    
    ### Look for 'equation' in the input JSON object; should be a text string.
    pStr = str(inputdata['equation'])

    outputStr = processInputStr(pStr)
    return json.dumps(outputStr)


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


def getTokenList(tokStr):
    '''
    Extract the arguments for proper display.
    Similar to the getInputArgumentList function but this one expects just a string with no = signs
    and parses without any special consideration of / or * signs
    '''

    tokStr = tokStr.replace('e^',' ')

    for i in ['=','+','-','*','/','(',')','{','}','[',']',',','^','\'']:
        tokStr = tokStr.replace(i, ' ')
    tmpList = tokStr.split()

    specialTokens = ['tf.math.pow','tf.math.exp', 'tf.gradients', 'stop_gradients','name', 'tf.math.log', 'tf.math.sqrt', 'tf.math.sin', 'tf.math.cos', 'tf.math.tan']
    mtokens = []
    for i in tmpList:
        if not isNumber(i) and (i not in mtokens) and (i not in specialTokens):
            mtokens.append(i)

    return mtokens

def getOperatorList(inStr):
    '''
    Extract only the operators from the input string and return them in a list
    '''

    listOfOperators = []
    for i in ['*','/','+','-','^']:
        if i in inStr:
            listOfOperators.append(i)

    return listOfOperators

def getRHSFromGradientStr(inStr):

    mitems = inStr.split('tf.gradients')
    trhs = mitems[1]
    matchIndex = trhs.find('stop_gradients')
    rhs = trhs[0:matchIndex]

    return rhs

def makePyFunction(inputVar, outputVar, functionName, codeStr):

    '''
    Create a Python function and return as a string
    '''
    
    inStr = ','.join(sorted(inputVar,key=str.lower))
    outStr = ','.join(sorted(outputVar,key=str.lower))

    # 4 spaces is ideal for indentation
    # construct the python function around the python snippet

    stringfun = '\ndef ' + functionName + '(' + inStr + '):'\
                    +'\n    ' + codeStr\
                    +'\n    return '+ outStr + '\n\n'

    return stringfun

def modifyEquation(inputStr, inputStrLHS, inputStrRHS, modifExpr, gradients, lhsRatio=0):

    '''
    Take an equation string and create a JSON object with the LHS and RHS components, etc.
    The return value is this JSON object
    '''
    modifExpr = modifExpr.replace('"','')
    if gradients == 1:
        
        ### For equations containing tf.gradient, there are essentially two equations, joined by a newline
        if '\n' in modifExpr:
            eqn1, eqn2 = modifExpr.split('\n')
            mArgs1 = []
            mArgs2 = []
            lhs1 = ""
            rhs1 = ""
            lhs2 = ""
            rhs2 = ""

            if 'gradients' in eqn1:
                eqn1 = eqn1.replace('"','')
                firstEqualSign = eqn1.find('=')
                gradRHS = getRHSFromGradientStr(eqn1)
                mArgs1 = getTokenList(gradRHS)
                lhs1 = eqn1[:firstEqualSign]
                rhs1 = eqn1[firstEqualSign+1:]
            if 'gradients' not in eqn2:
                lhs2, rhs2 = eqn2.split('=')
                eqn2 = eqn2.replace('"','')
                mArgs2 = getTokenList(rhs2)

            outputArgs = mArgs1 + mArgs2

            ### two equations on two lines; 4 space indent in front of the 2nd line
            lhs = lhs1 + '\n' + '    ' + lhs2
            rhs = rhs1 + '\n' + '    ' + rhs2
        else:
            lhs, rhs = modifExpr.split('=')
            outputArgs = getTokenList(lhs)
    elif lhsRatio == 0:
        lhs, rhs = modifExpr.split('=')
        outputArgs = getTokenList(lhs)
    elif lhsRatio == 1:
        totalExpr = inputStrLHS + ' = ' + modifExpr
        lhs, rhs = totalExpr.split('=')
        outputArgs = getTokenList(inputStrLHS)

    lhs = lhs.replace('"','').strip()
    rhs = rhs.replace('"','').strip()

    lhs = lhs.replace(' ','')

    mitems = []
    if '**' in lhs:
        mitems = lhs.split('**')
    elif '/' in lhs:
        mitems = lhs.split('/')
    ### IMPORTANT: keep the check for '*' after '**' else it will not work properly
    elif '*' in lhs:
        mitems = lhs.split('*')
    elif '^' in lhs:
        mitems = lhs.split('^')
    elif '+' in lhs:
        mitems = lhs.split('+')
    elif '-' in lhs:
        mitems = lhs.split('-')
    else:
        mitems = lhs.split()

    # Clean up variables such as (H1 and H2)
    mitems = [x.replace('(','').replace(')','') for x in mitems]
    
    ### Create the JSON object that will be returned in the end
    ### It has two top-level variables -  'text' and 'code
    modifObj = dict()
    modifObj['text'] = dict()
    modifObj['text']['type'] = []
    modifObj['text']['lhsOperators'] = []

    modifObj['code'] = []

    ### This is for the case when there is only one interpretation of an equation
    oneInterpretation = dict()

    tokList = []
    rhsStr = rhs
    tokList = getTokenList(rhsStr)

    operList = getOperatorList(lhs)
    modifObj['text']['lhsOperators'] = operList

    modifObj['text']['inputString'] = inputStr
    modifObj['text']['inputVars'] = tokList
    modifObj['text']['outputVars'] = outputArgs

    tCodeEqn = lhs + ' = ' + rhs

    gradString = 'gradient'
    noGradString = 'no_gradients'

    if gradients == 1:
        lhs1, lhs2 = lhs.split('\n')
        rhs1, rhs2 = rhs.split('\n')
        (modifObj['text']['type']).append(gradString)
    else:
        (modifObj['text']['type']).append(noGradString)

    inputArgs = getTokenList(inputStrRHS)

    modifObj['text']['outputExpression'] = list(str(inputStrLHS).split())

    lhsOperStr = ''
    tLHS = ''
    tRHS = ''

    listOutputVars = []

    lhsOperFlag = 0
    mulInterpretFlag = 0

    if (len(mitems) == 2) and ( ('/' in lhs) or ('*' in lhs) or ('+' in lhs) or ('-' in lhs) or ('^' in lhs) ) :

        lhsOperFlag = 1
        if isNumber(mitems[1]) and '^' in lhs:
            # case of a variable raised to a number, e.g., a ^ 2
            lhsOperStr = 'lhs_has_operators'

            listOutputVars = getTokenList(str(mitems[0]))
            tLHS = mitems[0]
            tRHS = 'tf.math.pow(' + rhs + ', 1/' + str(mitems[1]) + ')'
    
            oneInterpretation['inputVars'] = inputArgs
            oneInterpretation['outputVars'] = listOutputVars
    
        elif isNumber(mitems[0]) and '^' in lhs:
            # case of a number raised to a variable

            lhsOperStr = 'lhs_has_operators'

            listOutputVars = getTokenList(str(mitems[1]))
            tLHS = mitems[1]
            tRHS = 'tf.math.log(' + rhs + ', ' + str(mitems[0]) + ')'
    
            oneInterpretation['inputVars'] = inputArgs
            oneInterpretation['outputVars'] = listOutputVars
    
        else:
            # case of a variable raised to another variable, e.g., a ^ b

            mulInterpretFlag = 1
            lhsOperStr = 'lhs_has_operators'
        
            divideFlag = 0
            multiplyFlag = 0
            addFlag = 0
            subtractFlag = 0
            exponentFlag = 0

            if '/' in lhs:
                divideFlag = 1
            elif '*' in lhs:
                multiplyFlag = 1
            elif '+' in lhs:
                addFlag = 1
            elif '-' in lhs:
                subtractFlag = 1
            elif '^' in lhs:
                exponentFlag = 1

            modifObj['code'] = []

            ### INTERPRETATION 1 ###

            tLHS = mitems[0]

            if divideFlag == 1:
                tRHS = '(' + rhs + ')' + ' * ' + str(mitems[1])
            elif multiplyFlag == 1:
                tRHS = '(' + rhs + ')' + ' / ' + str(mitems[1])
            elif addFlag == 1:
                tRHS = '(' + rhs + ')' + ' - ' + str(mitems[1])
            elif subtractFlag == 1:
                tRHS = '(' + rhs + ')' + ' + ' + str(mitems[1])
            elif exponentFlag == 1:
                tRHS = 'tf.math.pow(' + rhs + ', 1/' + str(mitems[1]) + ')'

            totalExpr = tLHS + ' = ' + tRHS
            inputArgs = getTokenList(tRHS)
            interpretation1 = dict()
            if lhsOperStr not in modifObj['text']['type']:
                (modifObj['text']['type']).append(lhsOperStr)
            interpretation1['inputVars'] = inputArgs

            listOutputVars = getTokenList(str(mitems[0]))
            interpretation1['outputVars'] = listOutputVars

            tCodeEqn =  tLHS + ' = ' + tRHS

            interpretation1['tfCode'] = tCodeEqn
            interpretation1['pyCode'] = tCodeEqn
            interpretation1['npCode'] = tCodeEqn
            if 'tf.' in tCodeEqn:
                interpretation1['pyCode'] = interpretation1['tfCode'].replace('tf.','')
                interpretation1['npCode'] = interpretation1['tfCode'].replace('tf.math.','np.')
                if 'np.pow' in interpretation1['npCode']:
                    interpretation1['npCode'] = interpretation1['npCode'].replace('np.pow','np.power')

            inpVarsList = interpretation1['inputVars']
            outVarsList= interpretation1['outputVars']
            funcName = 'func_' + interpretation1['outputVars'][0]

            codeStr = interpretation1['tfCode']
            interpretation1['tfCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
            codeStr = interpretation1['pyCode']
            interpretation1['pyCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
            codeStr = interpretation1['npCode']
            interpretation1['npCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)

            modifObj['code'].append(interpretation1)

            ### INTERPRETATION 2 ###

            tLHS = mitems[1]
            if divideFlag == 1:
                tRHS = str(mitems[0]) + ' / ' + '(' + rhs + ')' 
            elif multiplyFlag == 1:
                tRHS = '(' + rhs + ')' + ' / ' + str(mitems[0])
            elif addFlag == 1:
                tRHS = '(' + rhs + ')' + ' - ' + str(mitems[0])
            elif subtractFlag == 1:
                tRHS = str(mitems[0]) + ' - ' + '(' + rhs + ')'
            elif exponentFlag == 1:
                tRHS = 'tf.math.log(' + rhs + ', ' + str(mitems[0]) + ')'

            totalExpr = tLHS + ' = ' + tRHS
            inputArgs = getTokenList(tRHS)
            interpretation2 = dict()
            if lhsOperStr not in modifObj['text']['type']:
                (modifObj['text']['type']).append(lhsOperStr)
            interpretation2['inputVars'] = inputArgs

            tCodeEqn =  tLHS + ' = ' + tRHS

            listOutputVars = getTokenList(str(mitems[1]))
            interpretation2['outputVars'] = listOutputVars
        
            interpretation2['tfCode'] = tCodeEqn
            interpretation2['pyCode'] = tCodeEqn
            interpretation2['npCode'] = tCodeEqn
            if 'tf.' in tCodeEqn:
                interpretation2['pyCode'] = interpretation2['tfCode'].replace('tf.','')
                interpretation2['npCode'] = interpretation2['tfCode'].replace('tf.math.','np.')
                if 'np.pow' in interpretation2['npCode']:
                    interpretation2['npCode'] = interpretation2['npCode'].replace('np.pow','np.power')

            inpVarsList = interpretation2['inputVars']
            outVarsList= interpretation2['outputVars']
            funcName = 'func_' +interpretation2['outputVars'][0]

            codeStr = interpretation2['tfCode']
            interpretation2['tfCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
            codeStr = interpretation2['pyCode']
            interpretation2['pyCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
            codeStr = interpretation2['npCode']
            interpretation2['npCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)

            modifObj['code'].append(interpretation2)


    elif len(mitems) == 1:
        listOutputVars.append(lhs)
        oneInterpretation['outputVars'] = listOutputVars
        oneInterpretation['inputVars'] = inputArgs


    if lhsOperFlag == 1 and mulInterpretFlag == 0:
        tCodeEqn =  tLHS + ' = ' + tRHS
        (modifObj['text']['type']).append(lhsOperStr)

    if mulInterpretFlag == 0 and gradients == 0:
        oneInterpretation['tfCode'] = tCodeEqn
        oneInterpretation['pyCode'] = tCodeEqn
        oneInterpretation['npCode'] = tCodeEqn
        if 'tf.' in tCodeEqn:
            oneInterpretation['pyCode'] = oneInterpretation['tfCode'].replace('tf.','')
            oneInterpretation['npCode'] = oneInterpretation['tfCode'].replace('tf.math.','np.')
            if 'np.pow' in oneInterpretation['npCode']:
                oneInterpretation['npCode'] = oneInterpretation['npCode'].replace('np.pow','np.power')

        inpVarsList = oneInterpretation['inputVars']
        outVarsList= oneInterpretation['outputVars']
        funcName = 'func_' + oneInterpretation['outputVars'][0]

        codeStr = oneInterpretation['tfCode']
        oneInterpretation['tfCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
        codeStr = oneInterpretation['pyCode']
        oneInterpretation['pyCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
        codeStr = oneInterpretation['npCode']
        oneInterpretation['npCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)

        modifObj['code'].append(oneInterpretation)

    if mulInterpretFlag == 0 and gradients == 1:
        oneInterpretation['tfCode'] = modifExpr
        oneInterpretation['pyCode'] = modifExpr
        oneInterpretation['npCode'] = modifExpr
        if 'tf.' in tCodeEqn:
            oneInterpretation['pyCode'] = oneInterpretation['tfCode'].replace('tf.','')
            oneInterpretation['npCode'] = oneInterpretation['tfCode'].replace('tf.math.','np.')
            if 'np.pow' in oneInterpretation['npCode']:
                oneInterpretation['npCode'] = oneInterpretation['npCode'].replace('np.pow','np.power')

        inpVarsList = oneInterpretation['inputVars']
        outVarsList= oneInterpretation['outputVars']
        funcName = 'func_' + oneInterpretation['outputVars'][0]

        codeStr = oneInterpretation['tfCode']
        oneInterpretation['tfCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
        codeStr = oneInterpretation['pyCode']
        oneInterpretation['pyCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)
        codeStr = oneInterpretation['npCode']
        oneInterpretation['npCode'] = makePyFunction(inpVarsList, outVarsList, funcName, codeStr)

        modifObj['code'].append(oneInterpretation)

    jsonObj = json.dumps(modifObj)
    return jsonObj


def processLnLogSqrt(lStr):
    '''
    For some operations in the input string, substitute with the corresponding tf.math.* operations
    '''

    logPattern = '[^a-zA-Z]*log\(.*'
    resultlog = re.search(logPattern, lStr)
    if resultlog:
        tmpLine = lStr.replace('log(','tf.math.log( ')
        lStr = tmpLine

    lnPattern = '[^a-zA-Z]*ln\(.*'
    resultln = re.search(lnPattern, lStr)
    if resultln:
        tmpLine = lStr.replace('ln(','tf.math.log( ')
        lStr = tmpLine

    sqrtPattern = '[^a-zA-Z]*sqrt\(.*'
    resultsqrt = re.search(sqrtPattern, lStr)
    if resultsqrt:
        tmpLine = lStr.replace('sqrt(','tf.math.sqrt( ')
        lStr = tmpLine

    asinPattern = '[^a-zA-Z]*asin\(.*'
    resultasin = re.search(asinPattern, lStr)
    if resultasin:
        tmpLine = lStr.replace('asin(','tf.math.asin( ')
        lStr = tmpLine

    sinPattern = '[^a-zA-Z]*sin\(.*'
    resultsin = re.search(sinPattern, lStr)
    if resultsin:
        tmpLine = lStr.replace('sin(','tf.math.sin( ')
        lStr = tmpLine

    cosPattern = '[^a-zA-Z]*acos\(.*'
    resultcos = re.search(cosPattern, lStr)
    if resultcos:
        tmpLine = lStr.replace('acos(','tf.math.acos( ')
        lStr = tmpLine

    cosPattern = '[^a-zA-Z]*cos\(.*'
    resultcos = re.search(cosPattern, lStr)
    if resultcos:
        tmpLine = lStr.replace('cos(','tf.math.cos( ')
        lStr = tmpLine

    tanPattern = '[^a-zA-Z]*atan\(.*'
    resulttan = re.search(tanPattern, lStr)
    if resulttan:
        tmpLine = lStr.replace('atan(','tf.math.atan( ')
        lStr = tmpLine

    tanPattern = '[^a-zA-Z]*tan\(.*'
    resulttan = re.search(tanPattern, lStr)
    if resulttan:
        tmpLine = lStr.replace('tan(','tf.math.tan( ')
        lStr = tmpLine

    return lStr


def extractExpression(inputStr, gradientFlag):
    '''
    Convert the given inputStr which is in text format, convert it into a code string
    '''

    currLine = inputStr.replace(' ','')
    currLine = inputStr.replace(' ','').replace('[','(').replace(']',')').replace('{','(').replace('}',')')

    ### Check for ln() or log() operators and convert as needed
    currLine = processLnLogSqrt(currLine)

    lenCurrLine = len(currLine)

    if gradientFlag == 1:
        strLHS = ""
        strRHS = ""
    else:
        if '=' in currLine:
            strLHS, strRHS = currLine.split('=')
        else:
            strLHS = ''
            strRHS = ''

    megaExpression = ""
    startEquationFlag = 1
    lhsProcessedFlag = 0
    specialExpCheckFlag = 0
    leftRightBracketsCount = 0
    doubleBracketsExponentFlag = 0
    specialUnaryOperatorFlag = 0
    powStrOpenFlag = 0
    mathexpWithPowFlag = 0
    if '^-' in currLine:
        specialUnaryOperatorFlag = 1
        
    mathPowFlag = 0
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
                leftRightBracketsCount += 1
            elif currChar in ['/','+','-']:
                megaExpression += " " + currChar + " " 
                if currChar == '-' and currLine[i-1] == '^':
                    specialUnaryOperatorFlag = 1
            elif currChar == '=':
                leftSide = megaExpression
                lhsProcessedFlag = 1
                megaExpression += " " + currChar + " " 
            elif currChar == ')':
                if nextChar == '(' or nextChar.isalpha():
                    megaExpression +=  " " + currChar + " * "
                else:
                    megaExpression +=  " " + currChar
                leftRightBracketsCount -= 1
            elif currChar == '}' and nextChar == '(':
                megaExpression += currChar + " * " 
                leftRightBracketsCount -= 1
            elif currChar == 'e' and nextChar =='^':
                megaExpression +=  " tf.math.exp"
                i = i+1
            elif currChar == '^':
                tfPowExpr = " tf.math.pow( "

                if currLine[i] == '^' and currLine[i-1] == ')':
                    doubleBracketsExponentFlag = 1

                k = i-1
                backCount = 1
                subStr = ""
                balancedBrackets = 0    # +1 for ) and -1 for (
                if currLine[k] == ')':
                    balancedBrackets += 1
                    k = k - 1
                    while (currLine[k] != '('):
                        if currLine[k] == ')':
                            balancedBrackets += 1
                        subStr += currLine[k]
                        k = k - 1
                        backCount += 1
                    
                    if balancedBrackets > 0:
                        while balancedBrackets != 0:
                            if currLine[k] == '(':
                                balancedBrackets -= 1
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
                        mathexpWithPowFlag = 0
                        if 'tf.math.exp' in megaExpression[p-11:p]:
                            p = p - 11
                            subStr2 = megaExpression[p:]
                            mathexpWithPowFlag = 1
                            
                        newExpr = megaExpression[0:p]
                        if (newExpr.strip())[-1] != '(':
                            tmpNewExpr = newExpr + '('
                            newExpr = tmpNewExpr
                        if mathexpWithPowFlag == 1:
                            megaExpression = newExpr + tfPowExpr + ' (' + subStr2 + ','
                        else:
                            megaExpression = newExpr + tfPowExpr + subStr2 + '),'
                            powStrOpenFlag = 1
                        specialExpCheckFlag = 1
                else:

                    while (currLine[k] != '('):
                        subStr += currLine[k]
                        k = k - 1
                        backCount += 1

                    subStr2 = subStr[::-1]

                    p = len(megaExpression) - 1

                    count=0
                    while megaExpression[p] != '(':
                        p = p - 1
                        count += 1
                    p = p - 1
                    newExpr = megaExpression[0:p]
                    if newExpr[-1] != '(':
                        tmpNewExpr = newExpr + '('
                        newExpr = tmpNewExpr
                    megaExpression = newExpr + tfPowExpr + subStr2 + ','
                    powStrOpenFlag = 1
                    
                    specialExpCheckFlag = 1

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
                        leftRightBracketsCount -= 1
                    else:
                        megaExpression += " " + currChar + " "
                elif currChar == '[' or currChar == '{':
                    megaExpression += " (  "
                    leftRightBracketsCount += 1
                elif currChar == ']' or currChar == '}':
                    if currChar == ']' and specialExpCheckFlag == 1:
                        megaExpression += " )) "
                        leftRightBracketsCount -= 2
                    else:
                        megaExpression += " ) "
                        leftRightBracketsCount -= 1
                else:
                    if specialExpCheckFlag == 1:
                        if isNumber(currChar):
                            megaExpression += currChar
                            i = i  + 1
                            currChar = currLine[i]
                            while isNumber(currChar) or (currChar == '.'):
                                megaExpression += currChar
                                i = i  + 1
                                currChar = currLine[i]
                            if currChar == '*':
                                megaExpression += ')' + currChar
                                leftRightBracketsCount -= 1
                            elif currChar == ']':
                                megaExpression += ') ) '
                                leftRightBracketsCount -= 2
                            elif currChar in ['/','+','-']:
                                megaExpression += ')' + currChar
                                leftRightBracketsCount -= 1
                                if currLine[i-1] == '^':
                                    specialUnaryOperatorFlag = 1
                            elif currChar == ')':
                                megaExpression += currChar 
                                leftRightBracketsCount -= 1
                                if powStrOpenFlag == 1 and specialUnaryOperatorFlag != 1:
                                    megaExpression += ')' 
                                    if mathexpWithPowFlag == 1:
                                        megaExpression += ')' 
                            else:
                                megaExpression += currChar + ')'
                                leftRightBracketsCount -= 1
                        else:
                            megaExpression += currChar + ')'
                            leftRightBracketsCount -= 1
                        specialExpCheckFlag = 0
                    else:
                        megaExpression += currChar
            i = i+1

        if specialUnaryOperatorFlag == 1:
            megaExpression += '))'
            leftRightBracketsCount -= 1
                
    return megaExpression, strLHS, strRHS


def processInputStr(inputStr):
    '''
    Process original input string. If it has multiple '=' signs, break it up and process each set separately
    '''

    returnObj = dict()

    returnObj['originalInpStr'] = inputStr
    if '=' in inputStr:
        mitems = inputStr.split('=')

    numItems = len(mitems)
    returnObj['numberOfEqns'] = numItems - 1

    returnObj['equations'] = []
    if numItems >= 2:

        count = 0
        while (count + 1) < numItems:

            eqStr = mitems[0] + ' = ' + mitems[count+1]
            eqRetObj = convertToCode(eqStr)
            #returnObj.append(eqRetObj)
            returnObj['equations'].append(eqRetObj)
            count += 1

    return returnObj


def convertToCode(inputStr):
    '''
    The main function that converts a text equation to equivalent code
    ''' 

    currLine = inputStr.replace(' ','')

    gradientFlag = 0
    lhsRatio = 0

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

        lhsRatioPatternDiv = '.*\/.*='

        result = re.search(lhsRatioPatternDiv, currLine)

        if result:
            # LHS has divide operation
            lhsRatio = 1

            endChar = result.end()
            subExpr = currLine[endChar:]
            (megaExpression, strLHS, strRHS) = extractExpression(subExpr, gradientFlag)
            strLHS = currLine[:endChar-1]
        else:
            lhsRatioPatternMul = '.*\*.*='
            result2 = re.search(lhsRatioPatternMul, currLine)

            if result2:
                # LHS has multiply operation
                lhsRatio = 1

                endChar = result2.end()
                subExpr = currLine[endChar:]

                (megaExpression, strLHS, strRHS) = extractExpression(subExpr, gradientFlag)
                strLHS = currLine[:endChar-1]
                strRHS = subExpr

            else:

                lhsRatioPatternPow = '.*\^.*='
                result3 = re.search(lhsRatioPatternPow, currLine)

                if result3:
                    # LHS has power operation
                    lhsRatio = 1

                    endChar = result3.end()
                    subExpr = currLine[endChar:]

                    (megaExpression, strLHS, strRHS) = extractExpression(subExpr, gradientFlag)
                    strLHS = currLine[:endChar-1]
                    strRHS = subExpr

                else:

                    (megaExpression, strLHS, strRHS) = extractExpression(inputStr, gradientFlag)

    modifObj = modifyEquation(inputStr,strLHS,strRHS,megaExpression,gradientFlag,lhsRatio)
    #print('\n###################################\nModified Object JSON object =\n', json.dumps(json.loads(modifObj),indent=4))

    return json.loads(modifObj)


@app.route('/')
def home():
    return 'Hello from main container'

if __name__ == "__main__":

    app.run(host="0.0.0.0", port="5002", debug=True)


