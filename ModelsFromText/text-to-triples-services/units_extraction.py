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

from typing import List
import json
import requests
import triple_store_query_execution as query
import spacy
from rdflib import Graph
import text_to_triples_service as t2t

variable_mappings = {0: 'entity',  1: 'equation', 2: 'variableName'}


class UnitsExtraction:
    triple_store_url = ''
    nlp_service_url = ''
    units_ontology_file = ''
    units_synonym_URI_str = ''
    query_execution = None
    units_ontology_graph_uri = ''
    nlp = None

    # Initializes appropriate variables with values from the config file
    def __init__(self, config):
        self.triple_store_url = config.TripleStoreURL
        self.nlp_service_url = config.NLPServiceURL
        self.units_ontology_file = config.UnitsOntologyPath
        self.units_synonym_URI_str = config.UnitsSynonymURIList
        self.query_execution = query.QueryExecution(config)
        self.units_ontology_graph_uri = config.UnitsOntologyGraphURI
        self.nlp = spacy.load('en_core_web_sm')

    # Main method to identify and extract units mentioned in text
    # also returns the scientific concept associated with the unit
    def extract_units(self, text: str, locality_uri: str, text_to_triples_obj: t2t.TextToTriples):

        g = text_to_triples_obj.get_graph(locality_uri)

        concept_mapper_service_url = self.nlp_service_url + '/conceptTaggingJSON'
        text = self.pre_process(text)

        input_info = self.get_request_payload(text)
        headers = {'Content-Type': 'application/json'}
        input_info_json = (json.dumps(input_info))

        r = requests.post(concept_mapper_service_url, input_info_json, headers=headers)
        response = r.json()
        unit_info_list: List[object] = []
        # parse the response
        # for unit, query and get the parent class
        # send it back
        if response is not None:
            print("\n")
            print(response)
            print("\n")
            for sent in response:
                concept_info_list = sent['conceptInfoList']
                for concept in concept_info_list:
                    unit_text = concept['name']
                    unit_uri = concept['uriStr']
                    # get parent class for uri
                    response_list = self.get_scientific_concept(unit_uri)

                    for unit_response in response_list:
                        if not unit_response["relatedConceptName"] == 'base unit':
                            unit_response["unit_uri"] = unit_uri

                            related_concept_name = str(unit_response["relatedConceptName"])
                            related_concept_name = related_concept_name.replace("unit", "").strip()

                            # use related concept name to search local graph
                            # ?x rdfs:label related_concept_name
                            # ?x semType ?eq
                            # return equation?
                            unit_context_list = self.get_equation(g, related_concept_name)

                            unit_info = {"unitText": unit_text, "unitName": unit_response["unitName"],
                                         "unitURI": unit_uri, "relatedConceptName": related_concept_name,
                                         "relatedConceptURI": unit_response["relatedConceptURI"],
                                         "start": concept["startByte"], "end": concept["endByte"],
                                         "unit_context": unit_context_list}
                            unit_info_list.append(unit_info)

        print(unit_info_list)
        return unit_info_list

    def get_equation(self, g: Graph, label: str) -> []:
        unit_context_list = []
        query_string = ("select ?entity ?equation ?variableName where {"
                        "?entity rdfs:label '" + label + "'. "
                        "OPTIONAL { "
                        "?augTypeObj <http://sadl.org/sadlimplicitmodel#semType> ?entity . "
                        "?dataDesc <http://sadl.org/sadlimplicitmodel#augmentedType> ?augTypeObj ."
                        "?dataDesc <http://sadl.org/sadlimplicitmodel#localDescriptorName> ?variableName ." 
                        "?list rdf:rest*/rdf:first ?dataDesc ."
                        "?equation a <http://sadl.org/sadlimplicitmodel#ExternalEquation> . "
                        "{?equation <http://sadl.org/sadlimplicitmodel#arguments> ?list .} "
                        "} }")

        print("\nlabel ... \n" + label + "\n")
        print("\nquery results ... \n")
        if g is not None:
            query_result = g.query(query_string)
            #for row in query_result:
            #   print(row)

            for row in query_result:
                unit_context = {}
                for i in range(0, len(row)):
                    if row[i] is not None:
                        unit_context[variable_mappings[i]] = row[i]
                unit_context_list.append(unit_context)
        print("\n")
        #g2 = None
        #query_result2 = g2.query(query_string)
        return unit_context_list

    # Executes SPARQL query against the Units Ontology in the triple store
    # returns unit uri, unit label, parent class uri and label
    # parent class represents the related scientific concept for the unit
    def get_scientific_concept(self, unit_uri):
        print("\n" + str(self.get_query_string(unit_uri, self.units_ontology_graph_uri)) + "\n")
        return self.query_execution.execute_query(self.get_query_string(unit_uri, self.units_ontology_graph_uri))

    @staticmethod
    def get_query_string(unit_uri, units_ontology_graph_uri):
        return ("select ?unitName ?relatedConceptURI ?relatedConceptName "
                "from <" + units_ontology_graph_uri + "> "
                "where"
                "{"
                "<" + unit_uri + "> rdfs:label ?unitName ."
                "<" + unit_uri + "> rdfs:subClassOf ?relatedConceptURI ."
                "?relatedConceptURI rdfs:label ?relatedConceptName ."
                "}")

    # Creates a payload for unit extraction (aka concept tagging) request
    def get_request_payload(self, text):
        paragraph_list: List[str] = [text]
        synonym_uri_list = self.units_synonym_URI_str.split("|")
        owl_line_list = self.get_owl_lines()

        dict_obj_list = [{'owlLineList': owl_line_list, 'synonymURIList': synonym_uri_list}]
        input_info = {'dictObjList': dict_obj_list, 'paragraphList': paragraph_list}
        return input_info

    # Loads the Units ontology into memory
    def get_owl_lines(self):
        owl_line = ''
        owl_line_list = []
        owl_file_path = self.units_ontology_file

        file = open(owl_file_path, 'r')
        for line in enumerate(file):
            line_str = (line[1].replace('"', '\"'))
            owl_line = owl_line + ' ' + line_str

        owl_line_list.append(owl_line.strip())
        return owl_line_list

    # Lemmatize the sentence
    def pre_process(self, text: str) -> str:
        doc = self.nlp(text)
        mod_text = ''

        for token in doc:
            print(token.lemma_, token.text)
            mod_text = mod_text + " " + token.lemma_

        return mod_text.strip()

