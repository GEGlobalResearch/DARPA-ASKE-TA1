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

import requests
import xml.etree.ElementTree as ET


def get_equation_var_context(body):
    equation_context = []

    query_string = get_query_string(body["variableName"], body["localityURI"])

    URL = "http://localhost:2420/sparql"
    PARAMS = {"query": query_string, format: "application/sparql-results/json"} #"format": "application%2Fsparql-results%2Bjson"

    r = requests.get(url=URL, params=PARAMS) #verify=False
    print(r.text, "\n")
    root = ET.fromstring(r.text)
    results = []
    if len(root) >= 1:
        results = root[1]

    for result in results:
        context = {}
        for binding in result:
            if len(binding) > 0:
                print(binding.attrib["name"], "\t", binding[0].text)
                context[binding.attrib["name"]] = binding[0].text
        equation_context.append(context)

    return equation_context


def get_query_string(variable_name, locality_uri):
    return ("select distinct ?variableName ?entityURI ?entityLabel ?equationString " 
            "from <" + locality_uri + "> "
            "where {"
                "?x <http://sadl.org/sadlimplicitmodel#descriptorName> ?variableName."
                "FILTER(REGEX(?variableName, '" + variable_name + "', 'i')) ."
                "OPTIONAL {?x <http://sadl.org/sadlimplicitmodel#augmentedType> ?augType ."
                "?augType <http://sadl.org/sadlimplicitmodel#semType> ?entityURI ."
                "?entityURI rdfs:label ?entityLabel  . }"
                "?list rdf:rest*/rdf:first ?x ."
                "?equation a <http://sadl.org/sadlimplicitmodel#ExternalEquation> ."
                "{?equation <http://sadl.org/sadlimplicitmodel#arguments> ?list .}" 
                "UNION"
                "{?equation <http://sadl.org/sadlimplicitmodel#returnTypes> ?list . }"
                "?equation <http://sadl.org/sadlimplicitmodel#expression> ?script ."
                "?script <http://sadl.org/sadlimplicitmodel#language> <http://sadl.org/sadlimplicitmodel#Text> ."
                "?script <http://sadl.org/sadlimplicitmodel#script> ?equationString ."
            "}")
