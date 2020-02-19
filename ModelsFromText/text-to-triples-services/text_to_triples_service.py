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
import nlp_services as nlp
from segtok.segmenter import split_single
from rdflib import Graph


class TextToTriples:
    model_file_path = ''
    model = None
    el = None
    page_cache = {}
    graphs_dictionary = {}
    domain_ontologies_dictionary = {}
    nlp_service_url = ''

    def __init__(self, config):
        self.model_file_path = config.NERModelFilePath

        # Load the NER model in memory
        self.model = extract.load_model(self.model_file_path)

        # Initialize Entity Linking Object
        self.el = entity_linking.EntityLinking(config)

        # Get NLP services URL
        self.nlp_service_url = config.NLPServiceURL

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
        phrases = {}
        print("Symbols\n")
        print(symbols)

        for sent in sentences:
            sent = sent.replace("\n", " ")
            sent = sent.strip()

            entity_dict = {}
            if sent is not '':

                # for noun chunk extraction
                mod_sent = sent

                # extract entities and equations
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
                        equation_string = entity["text"]
                        mod_sent = mod_sent.replace(equation_string, '')
                        self.process_scientific_equation(g, entity, equations, local_graph_uri, line)
                        equation_count = equation_count + 1

                # get noun chunks
                print("printing mod sentence ...\n")
                if mod_sent.strip() is not '':
                    print(mod_sent)
                    phrase_list = nlp.get_noun_chunks(self.nlp_service_url, mod_sent)
                    phrases[line] = phrase_list

                line = line + 1

        # For every variable (argument and return type), attach augmented types wherever possible
        # Augmented type links the variable to a wikidata concept, thereby semantically grounding it
        # self.add_augmented_types_to_graph(g, equations, symbols)
        self.add_augmented_types_phrase_based(g, equations, symbols, phrases)

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
    def add_augmented_types_phrase_based(g: Graph, equations, symbols, phrases):
        # loop through the equations
        # get the variables.
        # get the lines and get respective chunks
        # apply the filter method

        print("phrases\n")
        print(phrases)
        print()

        for equation in equations:

            eq_line = equation["line"]
            start = eq_line - 2
            end = eq_line + 2

            phrase_list = []
            for i in range(start, end):
                if i in phrases:
                    phrase_list.extend(phrases[i])

            eq_variables = []
            if "parameters" in equation:
                # parameters = equation["parameters"]
                if "text" in equation["parameters"]:
                    text_parameters = equation["parameters"]["text"]
                    if "inputVars" in text_parameters:
                        eq_variables.extend((text_parameters["inputVars"]))
                    if "outputVars" in text_parameters:
                        eq_variables.extend((text_parameters["outputVars"]))

            # apply the filter method
            var_phrase_mapping = get_var_phrase_mapping(phrase_list, eq_variables)

            # this list contains variable name and it's augmented type string.
            # we need a mapping between augmented trype string and its wikidata URI

            for var_phrase_tup in var_phrase_mapping:

                print("query for tup\n")
                print(var_phrase_tup)
                print()

                eq_var = var_phrase_tup[0]
                aug_type_text = var_phrase_tup[1]

                query_string = "SELECT ?uri where { ?uri <http://sadl.org/sadlimplicitmodel#localDescriptorName> \"" \
                               + eq_var + "\" }"

                query_result = g.query(query_string)
                for row in query_result:
                    print(row[0])
                    tp.populate_graph_entity_triples(g, None, aug_type_text, 0, aug_type_text)
                    tp.populate_augmented_type_triples(g, row[0], wikidata_uri=aug_type_text)

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

            print("eq variables")
            print(eq_variables)

            print("symbols")
            print(symbols)

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
                        # tp.populate_augmented_type_triples(g, row[0], wikidata_uri=symbol["entity_uri"])
                        tp.populate_augmented_type_triples(g, row[0], wikidata_uri=symbol["text"])

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

    def upload_domain_ontology(self, locality_uri: str, domain_ontology_str: str):
        message = 'ontology successfully uploaded'
        dg = Graph()
        dg.parse(data=domain_ontology_str, format="application/rdf+xml")

        # TODO: Error handling if the graph can't be parsed

        self.domain_ontologies_dictionary[locality_uri] = {"domain_ontology_string": domain_ontology_str,
                                                           "graph": dg}
        return message


def get_replace_chars():
    return ['.', '[', ']', '(', ')', ',', ';', ':', '!', "?", "\""]


def pre_process(string):
    replace = get_replace_chars()
    for char in replace:
        string = string.replace(char, "")
    return string.strip()


def remove_symbols(string):
    string = pre_process(string)
    mod = ''

    for char in string:
        if not (33 <= ord(char) <= 64):
            mod = mod + char
    return mod.strip()


def get_var_phrase_mapping(phrase_list, eq_var_list):
    var_phrase_mapping = []
    for var in eq_var_list:
        var_tok = var.split(' ')
        var_tok_len = len(var_tok)

        for phrase in phrase_list:
            phrase = pre_process(phrase)
            phrase_mod = get_phrase_tokens(phrase, var_tok_len)
            for p_mod in phrase_mod:
                if var is p_mod and '=' not in phrase:
                    print("mapping found\t" + var + "\t" + phrase)
                    # phrase_mod = phrase.replace(var, "")
                    phrase = remove_symbols(phrase)
                    if phrase_mod is not '':
                        phrase = get_phrase_concept_name(phrase, var)
                        print("tup saved \t" + var + "\t" + phrase)
                        var_phrase_mapping.append((var, phrase))
    return var_phrase_mapping


def get_phrase_concept_name(phrase: str, var: str):
    concept_name = ''
    phrase_tokens = phrase.split(' ')

    for phrase in phrase_tokens:
        if phrase is not var:
            concept_name = concept_name + phrase

    return concept_name


def get_phrase_tokens(phrase: str, length: int):
    phrase_tokens = phrase.split(' ')
    phrases_mod = []
    counter = 0
    for i in range(0, len(phrase_tokens)):
        phr = phrase_tokens[i]
        counter = counter + 1
        for j in range((i+1), len(phrase_tokens)):
            if counter < length:
                phr = phr + " " + phrase_tokens[j]
                counter = counter + 1
            else:
                counter = 0
                break
        phrases_mod.append(phr)
    return phrases_mod


# temp test method
def get_equations(g: Graph):
    # temp test call to print all equations
    query_string = """
    select ?x ?lang ?eqstring
    where 
    {
    ?x rdf:type <http://sadl.org/sadlimplicitmodel#ExternalEquation> .
    ?x <http://sadl.org/sadlimplicitmodel#expression> ?script .
    ?script <http://sadl.org/sadlimplicitmodel#language> ?lang .
    ?script <http://sadl.org/sadlimplicitmodel#script> ?eqstring .
    }
    """

    eq_list = {}
    query_result = g.query(query_string)
    for row in query_result:
        eq_uri = str(row[0])
        lang = str(row[1])
        lang = lang.replace("http://sadl.org/sadlimplicitmodel#", "")
        eq_string = str(row[2])

        if eq_uri not in eq_list.keys():
            eq_table = {lang: eq_string}
            eq_list[eq_uri] = eq_table
        else:
            eq_table = eq_list[eq_uri]
            eq_table[lang] = eq_string
            eq_list[eq_uri] = eq_table

    response = []
    for key in eq_list.keys():
        response.append(eq_list[key])

    print(eq_list)
    return response


# temp test method
def run_queries(g: Graph):
    query_string = """"
    SELECT ?desc
    WHERE
    {
        ?desc rdf:type <http://sadl.org/sadlimplicitmodel#DataDescriptor> .
    }
    """

    query_string = """
        select ?name
        where 
        {
        ?desc rdf:type <http://sadl.org/sadlimplicitmodel#DataDescriptor> .
        ?desc <http://sadl.org/sadlimplicitmodel#localDescriptorName> ?name .
        }
        """
    vars_list = []
    query_result = g.query(query_string)

    for row in query_result:
        vars_list.append(str(row[0]))

    return vars_list
