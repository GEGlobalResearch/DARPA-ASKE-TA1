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

import extract_concepts_equations as extract
import entity_linking
import triple_generation as tp
import equation_context as context
import text_to_python as t2p
from segtok.segmenter import split_single
from rdflib import Graph


class TextToTriples:
    model_file_path = ''
    model = None
    el = None
    page_cache = {}
    graphs_dictionary = {}

    def __init__(self, config):
        self.model_file_path = config.NERModelFilePath

        # Load the NER model in memory
        self.model = extract.load_model(self.model_file_path)

        # Initialize Entity Linking Object
        self.el = entity_linking.EntityLinking(config)

        self.page_cache = {}

        self.graphs_dictionary = {}

    def text_to_triples(self, body):

        concept_count = 0
        equation_count = 0

        # Break paragraphs into sentences
        sentences = split_single(body["text"])
        local_graph_uri = body["localityURI"]

        g = None
        if local_graph_uri in self.graphs_dictionary.keys():
            g = self.graphs_dictionary[local_graph_uri]
        else:
            g = Graph()

        line = 1
        symbols = {}
        equations = []

        print("Symbols\n")
        print(symbols)

        for sent in sentences:
            sent = sent.replace("\n", " ")
            sent = sent.strip()

            entity_dict = {}
            if sent is not '':
                entity_dict = extract.extract(self.model, sent)
                entities = []

                if len(entity_dict) > 0:
                    entities = entity_dict["entities"]

                for entity in entities:
                    if entity["type"] == "CONCEPT":
                        wikidata_entity = self.get_wikidata_alignment(entity)
                        self.generate_scientific_concept_triples(g, entity, wikidata_entity)
                        self.extract_equation_context(entity, wikidata_entity, sent, symbols, line)
                        concept_count = concept_count + 1
                    else:
                        self.process_scientific_equation(g, entity, equations, local_graph_uri, line)
                        equation_count = equation_count + 1
                line = line + 1

        # For every variable (argument and return type), attach augmented types wherever possible
        # Augmented type links the variable to a wikidata concept, thereby semantically grounding it
        self.add_augmented_types_to_graph(g, equations, symbols)

        # Update local graphs dictionary
        self.graphs_dictionary[local_graph_uri] = g

        self.save_graph(g)

        # TODO: How to send error message
        return {"numConceptsExtracted": concept_count, "numEquationsExtracted": equation_count}

    def get_wikidata_alignment(self, entity):
        # take label and call get_external_matching_entity
        search_str = entity["text"]
        search_str = pre_process(search_str)
        top_match_entity = {}

        if search_str.lower() in self.page_cache:
            top_match_entity = self.page_cache[search_str.lower()]
        else:
            top_match_entity = self.el.get_external_matching_entity(search_str)
            self.page_cache[search_str.lower()] = top_match_entity

        return top_match_entity

    @staticmethod
    def generate_scientific_concept_triples(g: Graph, entity, wikidata_entity):
        # triple generation
        tp.populate_graph_entity_triples(g=g, entity_uri=wikidata_entity["uri"],
                                         label=wikidata_entity["label"], similarity_score=wikidata_entity["score"],
                                         match_text=entity["text"])

    @staticmethod
    def extract_equation_context(entity: object, wikidata_entity: object, sent: str, symbols, line: int):
        # extract context for equation
        symbol_dict = context.eq_context_from_concept(entity, sent, line)
        if len(symbol_dict) > 0:
            symbol_dict["entity_uri"] = wikidata_entity["uri"]
            if line not in symbols:
                symbol_list = [symbol_dict]
                symbols[line] = symbol_list
            else:
                symbol_list = symbols[line]
                symbol_list.append(symbol_dict)
                symbols[line] = symbol_list

    @staticmethod
    def process_scientific_equation(g: Graph, entity, equations, local_graph_uri: str, line: str):
        equation_string = entity["text"]
        score = entity["confidence"]
        equation_parameters = t2p.text_to_python(equation_string)
        tp.populate_graph_equation_triples(g, equation_string, equation_parameters, local_graph_uri)

        equations.append({"equation": entity["text"], "line": line, "uri": "NA",
                          "parameters": equation_parameters})

    @staticmethod
    def add_augmented_types_to_graph(g: Graph, equations, symbols):
        # Loop through all equations
        # For each equation, check symbols in a window of 2 lines before and after
        # Get the augmented types
        for equation in equations:
            eq_line = equation["line"]
            start = eq_line - 2
            end = eq_line + 2

            # Get all the symbol objects for relevant lines in a list
            symbol_list = []
            for line_idx in range(start, end):
                if line_idx in symbols:
                    symbol_list.extend(symbols[line_idx])

            # Get all the variables (args and return types) in a single list
            eq_variables = []
            if "parameters" in equation:
                # parameters = equation["parameters"]
                if "text" in equation["parameters"]:
                    text_parameters = equation["parameters"]["text"]
                    if "inputVars" in text_parameters:
                        eq_variables.extend((text_parameters["inputVars"]))
                    if "outputVars" in text_parameters:
                        eq_variables.extend((text_parameters["outputVars"]))

            # if eq_var is in symbols, get the data_desc_uri
            # attach wikidata URI as augmented type
            for symbol in symbol_list:
                if str(symbol["symbol"]).strip() in eq_variables:

                    # TODO (for future): Make this into a prepared query
                    # TODO see: https://rdflib.readthedocs.io/en/stable/intro_to_sparql.html#prepared-queries
                    query_string = "SELECT ?uri where { ?uri <http://sadl.org/sadlimplicitmodel#localDescriptorName> \"" \
                                   + symbol["symbol"].strip() + "\" }"

                    query_result = g.query(query_string)
                    for row in query_result:
                        print(row[0])
                        tp.populate_augmented_type_triples(g, row[0], wikidata_uri=symbol["entity_uri"])

    def get_graph(self, local_graph_uri):
        g = None
        if local_graph_uri in self.graphs_dictionary.keys():
            g = self.graphs_dictionary[local_graph_uri]
        return g

    @staticmethod
    def save_graph(g: Graph):
        # TODO: How to return the triples back to the user?
        # TODO 1 Ans: Send the triples back as serialized string. Add a new API
        # TODO: How to return the triples back to the standalone demo UI?

        serialized_triples = "ERROR: Graph Not Found"
        serialization_format = "ERROR"

        if g is not None:
            serialized_triples = str(g.serialize(format='n3'))
            serialization_format = "n3"

            print("\nTriples\n")
            print(serialized_triples)
            print("\n")

        return {"triples": serialized_triples, "serializationFormat": serialization_format}

    def clear_graph(self, local_graph_uri):
        message = "Graph not found"
        if local_graph_uri in self.graphs_dictionary.keys():
            del self.graphs_dictionary[local_graph_uri]
            message = "Graph cleared"
        return {"message": message}


def get_replace_chars():
    return ['.', '[', ']', '(', ')', ',', ';', ':', '!', "?", "\""]


def pre_process(string):
    replace = get_replace_chars()
    for char in replace:
        string = string.replace(char, "")
    return string.strip()

