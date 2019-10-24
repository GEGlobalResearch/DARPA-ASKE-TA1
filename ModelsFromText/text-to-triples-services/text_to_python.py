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
    text_equation_parameters = {}
    code_equation_parameters = {}

    URL = "http://localhost:5002/convertTextToCode"
    body = {"equation": string_expression}
    PARAMS = json.dumps(body)
    headers = {'content-type': 'application/json; charset=UTF-8'}
    r = requests.get(url=URL, data=PARAMS, headers=headers)

    results = None

    try:
        results = r.json()
        print("\n")
        print(results)
    except json.decoder.JSONDecodeError:
        print("\n")
        print(string_expression)
        print("\n")

    if "text" in results:
        text = results["text"]
        if "inputVars" in text:
            text_equation_parameters["inputVars"] = text["inputVars"]
        if "outputVars" in text:
            text_equation_parameters["outputVars"] = text["outputVars"]

    if "code" in results:
        code = results["code"]
        if "pyCode" in code:
            code_equation_parameters["pyCode"] = code["pyCode"]
        if "tfCode" in code:
            code_equation_parameters["tfCode"] = code["tfCode"]
        if "inputVars" in code:
            code_equation_parameters["inputVars"] = code["inputVars"]
        if "outputVars" in code:
            return_vars = [code["outputVars"]]
            code_equation_parameters["outputVars"] = return_vars

    # if "codeEquation" in results:
    #   equation_parameters["codeEquation"] = results["codeEquation"]

    equation_results = {"text": text_equation_parameters, "code": code_equation_parameters}

    return equation_results
