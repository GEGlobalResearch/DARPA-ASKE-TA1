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
import text_to_triples_service as t2t
import triple_store_query_execution as query
import xml.etree.ElementTree as ET

variable_mappings = {0: 'variableName',  1: 'entityURI', 2: 'entityLabel', 3: 'equationURI'}


def local_variable_search(body, text_to_triples_obj: t2t.TextToTriples, config):
    context_list = []
    g = text_to_triples_obj.get_graph(body["localityURI"])
    message = 'Search Results from Graph ' + str(body["localityURI"])

    # TODO: Incorporate error message in the response
    if g is None:
        print("No graph found ... ")
        message = str(body["localityURI"]) + ' Graph not found'
        return {"message": message, "results": context_list}

    query_string = get_query_string_exact_match(body["variableName"])

    # Execute against graph g
    query_result = g.query(query_string)

    # If no results for exact match, then fuzzy match
    if len(query_result) == 0:
        query_string = get_query_string(body["variableName"], body["localityURI"])
        query_result = g.query(query_string)

    for row in query_result:
        context = {}
        for i in range(0, (len(row) - 1)):
            if row[i] is not None:
                context[variable_mappings[i]] = str(row[i])
        context_list.append(context)

    return {"message": message, "results": context_list}


def get_equation_var_context(body, config):
    query_execution = query.QueryExecution(config)
    equation_context = query_execution.execute_query(get_query_string(body["variableName"], body["localityURI"]))
    return equation_context


def temp(body):
    equation_context = []
    query_string = get_query_string(body["variableName"], body["localityURI"])
    URL = "http://localhost:2420/sparql"

    # "format": "application%2Fsparql-results%2Bjson"
    PARAMS = {"query": query_string, format: "application/sparql-results/json"}

    # verify=False
    r = requests.get(url=URL, params=PARAMS)
    print(r.text, "\n")
    root = ET.fromstring(r.text)
    results = []
    if len(root) >= 1:
        results = root[1]

    for result in results:
        context = {}
        for binding in result:
            if len(binding) > 0:
                # print(binding.attrib["name"], "\t", binding[0].text)
                context[binding.attrib["name"]] = binding[0].text
        equation_context.append(context)


def get_query_string_exact_match(variable_name: str):
    variable_name = variable_name.lower()
    return ("select distinct ?variableName ?entityURI ?entityLabel ?equation ?equationString" # equationString
            "where {"
            "?var_bnode <http://sadl.org/sadlimplicitmodel#localDescriptorName> ?variableName."
             "FILTER(lcase(?variableName) = '" + variable_name + "') ."
             "OPTIONAL {?var_bnode <http://sadl.org/sadlimplicitmodel#augmentedType> ?augType ."
             "?augType <http://sadl.org/sadlimplicitmodel#semType> ?entityURI ."
             "?entityURI rdfs:label ?entityLabel  . }"
             "?list rdf:rest*/rdf:first ?var_bnode ."
             "?equation ?prop ?list ."
             "?equation rdf:type <http://sadl.org/sadlimplicitmodel#ExternalEquation> ."
             "}")


def get_query_string(variable_name: str):
    variable_name = variable_name.lower()
    return ("select distinct ?variableName ?entityURI ?entityLabel ?equation ?equationString" # equationString
            "where {"
            "?var_bnode <http://sadl.org/sadlimplicitmodel#localDescriptorName> ?variableName."
             "FILTER(REGEX(?variableName, '" + variable_name + "', 'i')) ."
             "OPTIONAL {?var_bnode <http://sadl.org/sadlimplicitmodel#augmentedType> ?augType ."
             "?augType <http://sadl.org/sadlimplicitmodel#semType> ?entityURI ."
             "?entityURI rdfs:label ?entityLabel  . }"
             "?list rdf:rest*/rdf:first ?var_bnode ."
             "?equation ?prop ?list ."
             "?equation rdf:type <http://sadl.org/sadlimplicitmodel#ExternalEquation> ."
             "}")
