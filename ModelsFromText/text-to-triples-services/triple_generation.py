"""
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
"""

import requests
import xml.etree.ElementTree as ET
import datetime
import random
from rdflib import BNode, Graph, Literal, Namespace, RDF, RDFS, URIRef, XSD

entity_uri_attributes_cache = {}


def populate_graph_entity_triples(g: Graph, entity_uri: str, label: str, similarity_score: float,
                                  match_text: str) -> Graph:
    match_text = match_text.replace(".", "")

    # g.add((URIRef(entity_uri), RDFS.label, Literal(match_text)))
    # g.add((URIRef(entity_uri), RDFS.label, Literal(label)))
    # g = get_entity_attributes_graph(entity_uri, g)

    # match_text rdfs:subclassof sadl:ScientificConcept
    # need a method to convert "string" to valid URI
    # infer if its a UnitedQuantity
    # rdfs:label
    # rdfs:seealso wikidata
    # should we get more labels from wikidata?
    # same concept appears twice? (capture the context where it appears)
    # we need properties to capture the context

    sadl_implicit_model: Namespace = Namespace("http://sadl.org/sadlimplicitmodel#")
    match_text_uri_name = get_valid_uri(match_text)

    g.add((URIRef(match_text_uri_name), RDFS.subClassOf, sadl_implicit_model.ScientificConcept))
    g.add((URIRef(match_text_uri_name), RDFS.label, Literal(match_text)))
    g.add((URIRef(match_text_uri_name), RDFS.label, Literal(label)))

    if entity_uri is not None:
        g.add((URIRef(match_text_uri_name), RDFS.seeAlso, URIRef(entity_uri)))

    return g


def populate_graph_equation_triples(g: Graph, equation_string: str, parameters, local_graph_uri: str):
    sadl_implicit_model = Namespace("http://sadl.org/sadlimplicitmodel#")
    local_graph_uri = local_graph_uri + "#"

    python_code = "NA"
    tf_code = "NA"
    equation_type = ''
    inputs = []
    return_var = []

    if "type" in parameters:
        equation_type = parameters["type"]

    eq_num: int = get_equation_count(g) + 1
    base_eq_uri = "equation_" + str(eq_num)

    base_eq_uri = local_graph_uri + base_eq_uri
    g.add((URIRef(base_eq_uri), RDF.type, sadl_implicit_model.ExternalEquation))
    add_script_triples(g, sadl_implicit_model, base_eq_uri, "Text", equation_string)

    if "code" in parameters:
        codes = parameters["code"]
        for code_equation_parameters in codes:
            if "pyCode" in code_equation_parameters:
                python_code = code_equation_parameters["pyCode"]
            if "tfCode" in code_equation_parameters:
                tf_code = code_equation_parameters["tfCode"]
            if "inputVars" in code_equation_parameters:
                inputs = code_equation_parameters["inputVars"]
            if "outputVars" in code_equation_parameters:
                return_var = code_equation_parameters["outputVars"]

            if equation_type == "lhs_has_operators":
                eq_num: int = get_equation_count(g) + 1
                eq_uri = "equation_" + str(eq_num)
                eq_uri = local_graph_uri + eq_uri
                g.add((URIRef(eq_uri), RDF.type, sadl_implicit_model.ExternalEquation))
                # triple for connecting original equation with interpreted equation
                g.add((URIRef(eq_uri), sadl_implicit_model.derivedFrom, URIRef(base_eq_uri)))
            else:
                eq_uri = base_eq_uri

            # if type was set don't set Text triple
            # add_script_triples(g, sadl_implicit_model, eq_uri, "Text", equation_string)

            add_script_triples(g, sadl_implicit_model, eq_uri, "Python", python_code)
            add_script_triples(g, sadl_implicit_model, eq_uri, "Python-TF", tf_code)

            add_variable_triple(g, eq_uri, inputs, sadl_implicit_model, "arguments")
            add_variable_triple(g, eq_uri, return_var, sadl_implicit_model, "returnTypes")


def populate_augmented_type_triples(g: Graph, var_uri, wikidata_uri):
    # TODO: Move this to constructor
    sadl_implicit_model = Namespace("http://sadl.org/sadlimplicitmodel#")

    augmented_type_uri = BNode()
    g.add((var_uri, sadl_implicit_model.augmentedType, augmented_type_uri))
    g.add((augmented_type_uri, RDF.type, sadl_implicit_model.SemanticType))
    g.add((augmented_type_uri, sadl_implicit_model.semType, URIRef(wikidata_uri)))


def add_script_triples(g: Graph, sadl_implicit_model: Namespace, eq_uri, script_language: str, script_string: str):
    # TODO: Get SADL model string from constructor
    python_tf_uri = "http://sadl.org/sadlimplicitmodel#Python-TF"

    script_uri = BNode()
    g.add((URIRef(eq_uri), sadl_implicit_model.expression, script_uri))
    g.add((script_uri, RDF.type, sadl_implicit_model.Script))

    if script_language == "Text":
        g.add((script_uri, sadl_implicit_model.language, sadl_implicit_model.Text))
    elif script_language == "Python":
        g.add((script_uri, sadl_implicit_model.language, sadl_implicit_model.Python))
    elif script_language == "Python-TF":
        g.add((script_uri, sadl_implicit_model.language, URIRef(python_tf_uri)))

    # if script_string is not "NA":
    g.add((script_uri, sadl_implicit_model.script, Literal(script_string)))


def add_variable_triple(g: Graph, eq_uri, var_list, sadl_implicit_model: Namespace, prop_name: str):
    list_start = BNode()
    list_rest = BNode()

    if prop_name == "arguments":
        g.add((URIRef(eq_uri), sadl_implicit_model.arguments, list_start))
    elif prop_name == "returnTypes":
        g.add((URIRef(eq_uri), sadl_implicit_model.returnTypes, list_start))

    for var in var_list:
        var_uri = BNode()
        g.add((var_uri, RDF.type, sadl_implicit_model.DataDescriptor))
        g.add((var_uri, sadl_implicit_model.localDescriptorName, Literal(var)))
        # g.add((var_uri, sadl_implicit_model.dataType, sadl_implicit_model.anyDataType))
        g.add((var_uri, sadl_implicit_model.dataType, XSD.decimal))
        g.add((list_start, RDF.first, var_uri))
        g.add((list_start, RDF.rest, list_rest))
        list_start = list_rest
        list_rest = BNode()


def get_equation_count(g: Graph):
    num_equations = 0

    query_string = "select (count (?eq) as ?count) where " \
                   "{ ?eq rdf:type <http://sadl.org/sadlimplicitmodel#ExternalEquation> }"
    query_result = g.query(query_string)

    for row in query_result:
        num_equations = row[0]
    print(num_equations)

    return num_equations


def get_valid_uri(uri_name: str) -> str:
    uri_name = uri_name.replace(' ', '_')
    mod_uri_name = ''

    for char in uri_name:
        code = ord(char)
        if 48 <= code <= 57 or 65 <= code <= 90 or 97 <= code <= 122:
            mod_uri_name = mod_uri_name + char

    return mod_uri_name

# Backward compatible methods. To to be deprecated and removed #


def generate_entity_triples(uri, label, similarity_score, match_text):
    triples = []

    match_text_uri = "_:match_text_" + str(id(match_text)) + str(id(random.random()))

    triples.append(get_triple(match_text_uri, "<http://www.w3.org/2002/07/owl#sameAs>", "<" + uri + ">"))

    triple = {"subject": "<" + uri + ">", "predicate": "<http://www.w3.org/2000/01/rdf-schema#label>",
              "object": label, "tripleConfScore": similarity_score}
    triples.append(triple)
    triples.extend(get_entity_attributes(uri))
    return triples


def generate_equation_triples(equation_string, parameters):
    triples = []

    uri = "_:eq_" + str(id(equation_string))

    triples.append(get_triple(uri, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
                              "<http://sadl.org/sadlimplicitmodel#ExternalEquation>"))

    script_text_uri = "_:script_text_" + str(id(equation_string))
    triples.extend(get_script_triples(uri, script_text_uri, equation_string, "Text"))

    script_python_uri = "_:script_python_" + str(id(equation_string))
    python_code = "NA"
    if "pyCode" in parameters:
        python_code = parameters["pyCode"]
    triples.extend(get_script_triples(uri, script_python_uri, python_code, "Python"))

    # Data Desc object for each var
    inputs = []
    if "inputVars" in parameters:
        inputs = parameters["inputVars"]

    list_start = "_:list_" + str(id("start")) + "_" + str(id(equation_string))
    list_rest = "_:list_" + str(id("rest")) + "_" + str(id(equation_string))

    triples.append(get_triple(uri, "<http://sadl.org/sadlimplicitmodel#arguments>", list_start))

    for var in inputs:
        triples.extend(get_data_desc_triples(id(equation_string), var, list_start, list_rest, "_:data_"))
        list_start = list_rest
        list_rest = "_:list_" + str(id("rest")) + str(id(list_rest))

    # add nil for last list_rest?

    return_var = []
    if "returnVar" in parameters:
        return_var = parameters["returnVar"]

    list_start = "_:list_return_" + str(id("start")) + "_" + str(id(equation_string))
    list_rest = "_:list_return_" + str(id("rest")) + "_" + str(id(equation_string))

    triples.append(get_triple(uri, "<http://sadl.org/sadlimplicitmodel#returnTypes>", list_start))

    for var in return_var:
        triples.extend(get_data_desc_triples(id(equation_string), var, list_start, list_rest, "_:return_"))
        list_start = list_rest
        list_rest = "_:list_return_" + str(id("rest")) + str(id(list_rest))

    return triples


def get_data_desc_triples(eq_id, var, list_start, list_rest, prefix):
    triples = []
    var_uri = prefix + str(id(var)) + "_" + str(eq_id)

    triples.append(get_triple(var_uri, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
                              "<http://sadl.org/sadlimplicitmodel#DataDescriptor>"))
    triples.append(get_triple(var_uri, "<http://sadl.org/sadlimplicitmodel#localDescriptorName>", "\"" + var + "\""))
    triples.append(get_triple(var_uri, "<http://sadl.org/sadlimplicitmodel#dataType>",
                              "<http://sadl.org/sadlimplicitmodel#anyDataType>"))
    triples.append(get_triple(list_start, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>", var_uri))
    triples.append(get_triple(list_start, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>", list_rest))

    # return {"data_uri": var_uri, "triples": triples}
    return triples


def get_script_triples(eq_uri, script_uri, script_str, script_language):
    triples = [get_triple(eq_uri, "<http://sadl.org/sadlimplicitmodel#expression>", script_uri),

               get_triple(script_uri, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
                          "<http://sadl.org/sadlimplicitmodel#Script>"),

               get_triple(script_uri, "<http://sadl.org/sadlimplicitmodel#language>",
                          "<http://sadl.org/sadlimplicitmodel#" + script_language + ">"),

               get_triple(script_uri, "<http://sadl.org/sadlimplicitmodel#script>",
                          "\"" + script_str + "\"")]

    return triples


def generate_equation_triples_v1(equation_string):
    triples = []
    uri = "_:eq_" + str(id(equation_string))
    triple = {"subject": uri, "predicate": "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
              "object": "<http://sadl.org/sadlimplicitmodel#ExternalEquation>",
              "tripleConfScore": 1.0}

    triples.append(triple)
    triple = {"subject": uri, "predicate": "<http://sadl.org/sadlimplicitmodel#expression>",
              "object": equation_string, "tripleConfScore": 1.0}
    triples.append(triple)

    equation_info = {"uri": uri, "triples": triples}

    # return triples
    return equation_info


def get_triple(subject, predicate, triple_object):
    return {"subject": subject, "predicate": predicate, "object": triple_object, "tripleConfScore": 1.0}


# TODO: Reuse methods in triple_store_query_execution.py
def get_entity_attributes_graph(entity_uri: str, g: Graph) -> Graph:
    query = get_entity_attributes_query(entity_uri)

    URL = "https://query.wikidata.org/sparql"
    PARAMS = {"query": query}

    results = []
    if entity_uri in entity_uri_attributes_cache:
        results = entity_uri_attributes_cache[entity_uri]
    else:
        r = requests.get(url=URL, params=PARAMS, verify=False)
        try:
            root = ET.fromstring(r.text)
            if len(root) >= 1:
                results = root[1]
                entity_uri_attributes_cache[entity_uri] = results
        except ET.ParseError:
            print(r.text)

    g = process_query_results(entity_uri, results, g)

    return g


def process_query_results(entity_uri: str, results, g: Graph) -> Graph:
    wikidata_prop_namespace = Namespace("http://www.wikidata.org/prop/direct/")

    for res in results:
        entity_type = {}
        entity_subclass_of = {}
        entity_property = {}

        for elem in res:
            element_type = elem.attrib["name"]
            element_value = elem[0].text

            if element_type == "entityType":
                entity_type["uri"] = element_value
            elif element_type == "entityTypeLabel":
                entity_type["label"] = element_value
            elif element_type == "entitySubClassOf":
                entity_subclass_of["uri"] = element_value
            elif element_type == "entitySubClassOfLabel":
                entity_subclass_of["label"] = element_value
            elif element_type == "wikidataProperty":
                entity_property["uri"] = element_value
            elif element_type == "wikidataPropertyLabel":
                entity_property["label"] = element_value

        if len(entity_type) == 2:
            g.add((URIRef(entity_uri), RDF.type, URIRef(entity_type["uri"])))
            g.add((URIRef(entity_type["uri"]), RDFS.label, Literal(entity_type["label"])))
        elif len(entity_subclass_of) == 2:
            g.add((URIRef(entity_uri), RDFS.subClassOf, URIRef(entity_subclass_of["uri"])))
            g.add((URIRef(entity_subclass_of["uri"]), RDFS.label, Literal(entity_subclass_of["label"])))

        elif len(entity_property) == 2:
            g.add((URIRef(entity_uri), wikidata_prop_namespace.P1687, URIRef(entity_property["uri"])))
            g.add((URIRef(entity_property["uri"]), RDFS.label, Literal(entity_property["label"])))

    # clear cache if filled up too much
    if len(entity_uri_attributes_cache) > 500:
        entity_uri_attributes_cache.clear()

    return g


def get_entity_attributes(entity_uri):
    query = get_entity_attributes_query(entity_uri)

    URL = "https://query.wikidata.org/sparql"
    PARAMS = {"query": query}

    results = []

    if entity_uri in entity_uri_attributes_cache:
        results = entity_uri_attributes_cache[entity_uri]
    else:
        r = requests.get(url=URL, params=PARAMS, verify=False)
        try:
            root = ET.fromstring(r.text)
            if len(root) >= 1:
                results = root[1]
                entity_uri_attributes_cache[entity_uri] = results
        except ET.ParseError:
            print(r.text)

    triples = []

    for res in results:
        entity_type = {}
        entity_subclass_of = {}
        entity_property = {}

        for elem in res:
            element_type = elem.attrib["name"]
            element_value = elem[0].text

            if element_type == "entityType":
                entity_type["uri"] = element_value
            elif element_type == "entityTypeLabel":
                entity_type["label"] = element_value
            elif element_type == "entitySubClassOf":
                entity_subclass_of["uri"] = element_value
            elif element_type == "entitySubClassOfLabel":
                entity_subclass_of["label"] = element_value
            elif element_type == "wikidataProperty":
                entity_property["uri"] = element_value
            elif element_type == "wikidataPropertyLabel":
                entity_property["label"] = element_value

        if len(entity_type) == 2:
            triples.append(get_triple("<" + entity_uri + ">", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
                                      "<" + entity_type["uri"] + ">"))
            triples.append(get_triple("<" + entity_type["uri"] + ">", "<http://www.w3.org/2000/01/rdf-schema#label>",
                                      entity_type["label"]))
        elif len(entity_subclass_of) == 2:
            triples.append(get_triple("<" + entity_uri + ">", "<http://www.w3.org/2000/01/rdf-schema#subClassOf>",
                                      "<" + entity_subclass_of["uri"] + ">"))
            triples.append(get_triple("<" + entity_subclass_of["uri"] + ">",
                                      "<http://www.w3.org/2000/01/rdf-schema#label>", entity_subclass_of["label"]))
        elif len(entity_property) == 2:
            triples.append(get_triple("<" + entity_uri + ">", "<http://www.wikidata.org/prop/direct/P1687>",
                                      "<" + entity_property["uri"] + ">"))
            triples.append(get_triple("<" + entity_property["uri"] + ">",
                                      "<http://www.w3.org/2000/01/rdf-schema#label>", entity_property["label"]))

    # clear cache if filled up too much
    if len(entity_uri_attributes_cache) > 500:
        entity_uri_attributes_cache.clear()

    return triples


def get_entity_attributes_query(entity_uri: str) -> str:
    if 'http' not in entity_uri:
        entity_uri = 'http://www.wikidata.org/entity/' + entity_uri

    query = ("select distinct * where {"
             "{"
             "  <" + entity_uri + "> <http://www.wikidata.org/prop/direct/P31> ?entityType ."
                                  "  ?entityType rdfs:label ?entityTypeLabel ."
                                  "  FILTER (lang(?entityTypeLabel) = 'en') ."
                                  "}"
                                  "UNION"
                                  "{"
                                  "  <" + entity_uri + "> <http://www.wikidata.org/prop/direct/P279> ?entitySubClassOf ."
                                                       "  ?entitySubClassOf rdfs:label ?entitySubClassOfLabel ."
                                                       "  FILTER (lang(?entitySubClassOfLabel) = 'en') ."
                                                       "}"
                                                       "UNION"
                                                       "{"
                                                       "  <" + entity_uri + "> <http://www.wikidata.org/prop/direct/P1687> ?wikidataProperty ."
                                                                            " ?wikidataProperty rdfs:label ?wikidataPropertyLabel ."
                                                                            " FILTER (lang(?wikidataPropertyLabel) = 'en') ."
                                                                            "}"
                                                                            "}")
    return query


def get_equation_context_triples(data_desc_uri, wikidata_uri, rand):
    triples = []

    aug_sem_type_uri = "_:aug_sem_type_" + str(rand) + "_" + str(random.random())

    triples.append(get_triple(data_desc_uri, "<http://sadl.org/sadlimplicitmodel#augmentedType>", aug_sem_type_uri))
    triples.append(get_triple(aug_sem_type_uri, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
                              "<http://sadl.org/sadlimplicitmodel#SemanticType>"))
    triples.append(get_triple(aug_sem_type_uri, "<http://sadl.org/sadlimplicitmodel#semType>",
                              "<" + wikidata_uri + ">"))
    return triples


def get_equation_context_triples_depricated(arg_name, entity_uri, equation_uri, parameters):
    triples = []
    arg_uri = "arg_" + arg_name + str(datetime.datetime.now())
    arg_uri = id(arg_uri)
    arg_uri = "_:arg_" + str(arg_uri)

    # base uri http://sadl.org/sadlimplicitmodel#
    # I have the equation uri ->  arguments, returnTypes

    triple = {"subject": arg_uri, "predicate": "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
              "object": "<http://sadl.org/sadlimplicitmodel#Arugument>",
              "tripleConfScore": 1.0}

    triples.append(triple)

    triple = {"subject": arg_uri, "predicate": "<http://sadl.org/sadlimplicitmodel#argName>",
              "object": arg_name,
              "tripleConfScore": 1.0}

    triples.append(triple)

    # aug_type_uri = "aug_type_" + arg_name + str(datetime.datetime.now()) #entity_uri replaced with agr_name
    # aug_type_uri = id(aug_type_uri)
    # aug_type_uri = "_:aug_type_" + str(aug_type_uri)

    aug_type_uri = "_:aug_type_" + arg_uri.replace("_:arg_", "")

    triple = {"subject": aug_type_uri, "predicate": "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
              "object": "<http://sadl.org/sadlimplicitmodel#SemanticType>",
              "tripleConfScore": 1.0}

    triples.append(triple)

    triple = {"subject": aug_type_uri, "predicate": "<http://sadl.org/sadlimplicitmodel#semType>",
              "object": "<" + entity_uri + ">",
              "tripleConfScore": 1.0}

    triples.append(triple)

    triple = {"subject": arg_uri, "predicate": "<http://sadl.org/sadlimplicitmodel#augmentedType>",
              "object": aug_type_uri,
              "tripleConfScore": 1.0}

    triples.append(triple)

    print("\n", arg_name, entity_uri, aug_type_uri, "\n", triple, "\n")

    return triples
