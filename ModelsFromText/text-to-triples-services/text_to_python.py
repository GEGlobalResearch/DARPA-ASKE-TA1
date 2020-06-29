'''
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
'''

import json
import requests


def text_to_python(string_expression):

    URL = "http://localhost:5002/convertTextToCode"
    body = {"equation": string_expression}
    PARAMS = json.dumps(body)
    headers = {'content-type': 'application/json; charset=UTF-8'}
    r = requests.get(url=URL, data=PARAMS, headers=headers)

    results = None

    try:
        results = r.json()
    except json.decoder.JSONDecodeError:
        print("\n Failed conversion to Python ...\n")
        print(string_expression)
        print("\n")

    equation_results = []
    if results is not None:

        if "equations" in results:
            equations = results["equations"]

            for equation in equations:
                equation_info = process_equation(equation)
                equation_results.append(equation_info)

    # equation_results = {"text": text_equation_parameters, "code": code_equation_parameters, "type": equation_type}

    return equation_results


def process_equation(equation):
    text_equation_parameter = {}
    code_equation_parameters = []
    equation_type = ''

    # TODO: How to leverage "type" information (e.g. lhs_has_operators)
    if "text" in equation:
        text = equation["text"]
        if "inputVars" in text:
            text_equation_parameter["inputVars"] = text["inputVars"]
        if "outputVars" in text:
            text_equation_parameter["outputVars"] = text["outputVars"]
        if "outputExpression" in text:
            text_equation_parameter["outputExpression"] = text["outputExpression"]
        if "type" in text:
            type_arr = text["type"]
            if "lhs_has_operators" in type_arr:
                equation_type = "lhs_has_operators"

    if "code" in equation:
        codes = equation["code"]
        # TODO: multiple interpretations. Need to keep track ...
        # TODO: What happens to triple generation for multiple interpretations?
        for code in codes:
            code_equation_parameter = {}
            if "pyCode" in code:
                code_equation_parameter["pyCode"] = code["pyCode"]
            if "tfCode" in code:
                code_equation_parameter["tfCode"] = code["tfCode"]
            if "npCode" in code:
                code_equation_parameter["npCode"] = code["npCode"]
            if "inputVars" in code:
                code_equation_parameter["inputVars"] = code["inputVars"]
            if "outputVars" in code:
                code_equation_parameter["outputVars"] = code["outputVars"]

            code_equation_parameters.append(code_equation_parameter)

    equation_info = {"text": text_equation_parameter, "code": code_equation_parameters, "type": equation_type}
    return equation_info
