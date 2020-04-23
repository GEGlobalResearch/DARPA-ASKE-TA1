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
import concept_mapper_domain_ontology as concept_mapper
from segtok.segmenter import split_single
from rdflib import Graph, Literal, RDFS, URIRef
import string_utils
import urllib.parse
import automates


class TextToTriples:
    model_file_path = ''
    model = None
    el = None
    concept_mapper_obj = None
    page_cache = {}
    graphs_dictionary = {}
    domain_ontologies_dictionary = {}
    nlp_service_url = ''
    automates_obj = None

    def __init__(self, config):
        self.model_file_path = config.NERModelFilePath

        # Load the NER model in memory
        self.model = extract.load_model(self.model_file_path)

        # Initialize Entity Linking Object
        self.el = entity_linking.EntityLinking(config)

        # Get NLP services URL
        self.nlp_service_url = config.NLPServiceURL

        # Initialize Concept Mapper service object
        self.concept_mapper_obj = concept_mapper.ConceptMapperDomainOntology(config)

        self.automates_obj = automates.Automates(config)

        self.page_cache = {}

        self.graphs_dictionary = {}

        self.domain_ontologies_dictionary = {}

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

        base_uri = "http://temp#"

        if local_graph_uri in self.domain_ontologies_dictionary:
            domain_ontology_obj = self.domain_ontologies_dictionary[local_graph_uri]
            if 'base_uri' in domain_ontology_obj:
                base_uri = domain_ontology_obj['base_uri']

        line = 1
        symbols = {}
        equations = []
        phrases = {}

        automates_variable_definitions: list = []
        domain_concepts: list = []
        data_driven_concepts: list = []
        np_concepts: list = []

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

                        data_driven_concepts.append({"entity": entity, "wikidata": wikidata_entity})

                        # domain_ontology_info = self.get_domain_ontology_alignment(local_graph_uri, entity["text"])

                        # self.generate_scientific_concept_triples(g, entity, wikidata_entity,
                        #                                         domain_ontology_info, base_uri)
                        self.extract_equation_context(entity, wikidata_entity, sent, symbols, line)

                        # concept_count = concept_count + 1
                    else:
                        equation_string = entity["text"]
                        mod_sent = mod_sent.replace(equation_string, '')
                        self.process_scientific_equation(g, entity, equations, line, base_uri)
                        equation_count = equation_count + 1

                # get noun chunks
                # print("printing mod sentence ...\n")
                if mod_sent.strip() is not '':
                    # print(mod_sent)
                    phrase_list = nlp.get_noun_chunks(self.nlp_service_url, mod_sent)
                    # get wikidata entities
                    data_driven_concepts.extend(self.add_noun_phrase_scientific_concept(None, phrase_list, None))
                    phrases[line] = phrase_list

                line = line + 1

                # check if sentence has terms from domain ontology
                domain_concepts.extend(self.get_domain_concepts(local_graph_uri, sent))

                # extract variable definition via automates
                json_response = self.automates_obj.call_service(sent)
                automates_variable_definitions.extend(automates.extract_from_json(json_response))

                # align domain concepts
                # domain_ontology_info = self.get_domain_ontology_alignment(local_graph_uri, sent)
                # if 'numConcepts' in domain_ontology_info:
                #    concept_count = concept_count + domain_ontology_info['numConcepts']
                # self.generate_scientific_concept_triples(g, {}, {}, domain_ontology_info, base_uri)

        # the concept in this tuple needs to be aligned wikidata and/or domain model
        # if not present in either, new concept is found
        # can we send the same sentence to AutoMATES to get an additional table before doing the alignment

        print("\npre-merge automates response")
        print(automates_variable_definitions)

        # tuple structure: (eq_var, aug_type_text, eq_uri)
        var_phrase_eq_uri_map = self.get_augmented_types_phease_based(g, equations, phrases)
        un_mapped_augmented_types = []

        # merge augmented types extracted by text to triples with automates
        var_eq_uri_tmp_map = []
        for tup in var_phrase_eq_uri_map:
            var_name = tup[0]
            aug_type_text = tup[1]
            var_align = False

            for var_def in automates_variable_definitions:
                a_var_align = False
                for a_var_name in var_def:
                    a_var_text = var_def[a_var_name]

                    if var_name.lower() == a_var_name.lower():
                        a_var_align = True
                        var_align = True
                        if len(aug_type_text) > len(a_var_text):
                            var_eq_uri_tmp_map.append((var_name, aug_type_text, tup[2]))
                        else:
                            var_eq_uri_tmp_map.append((var_name, a_var_text, tup[2]))

                if a_var_align:
                    automates_variable_definitions.remove(var_def)

            # t2t variable did not find a match in automates augmented types
            if not var_align:
                var_eq_uri_tmp_map.append((var_name, aug_type_text, tup[2]))

        # automates variables did not find a match in t2t augmented types
        for var_def in automates_variable_definitions:
            for a_var_name in var_def:
                eq_uris = get_eq_uri_for_var(g, a_var_name)
                for uri in eq_uris:
                    var_eq_uri_tmp_map.append((a_var_name, var_def[a_var_name], uri))

        var_phrase_eq_uri_map = var_eq_uri_tmp_map

        for tup in var_phrase_eq_uri_map:
            aug_type_text = tup[1]
            wikidata_entity = self.get_wikidata_alignment({"text": aug_type_text})

            if len(wikidata_entity) > 0:
                data_driven_concepts.append({"entity": {"text": aug_type_text}, "wikidata": wikidata_entity,
                                             "augmented_type": {"name": tup[1], "eq_uri": tup[2]}})

            dc_arr = self.get_domain_concepts(local_graph_uri, aug_type_text)
            for dc in dc_arr:
                dc["augmented_type"] = {"name": tup[1], "eq_uri": tup[2]}

            domain_concepts.extend(dc_arr)

            if len(wikidata_entity) == 0 and len(dc_arr) == 0:
                un_mapped_augmented_types.append({"name": tup[1], "eq_uri": tup[2]})

            # what happens to concepts that don't get linked?
            # tp.populate_augmented_type_triples(g, row[0], local_name)

        print("\ndata concepts")
        print(data_driven_concepts)
        print("\ndomain concepts")
        print(domain_concepts)
        print("\n")
        print("\naugmented types not mapped")
        print(un_mapped_augmented_types)
        print("\n")
        print("\nautomates response")
        print(automates_variable_definitions)
        print("\n")

        concept_count = 0

        # Merge named data-driven concepts with domain concepts
        for data_concept in data_driven_concepts:
            entity = data_concept["entity"]
            wikidata_entity = data_concept["wikidata"]
            entity_name: str = entity["text"]

            merge_found = False
            augmented_type = {}
            if "augmented_type" in data_concept:
                augmented_type = data_concept["augmented_type"]

            for domain_concept in domain_concepts:
                domain_name: str = domain_concept["name"]
                domain_concept["merge"] = False

                if entity_name.lower() == domain_name.lower():
                    # generate triples
                    domain_concept["merge"] = True
                    self.add_merged_entity_to_graph(g, base_uri, entity, wikidata_entity, domain_concept,
                                                    augmented_type)
                    merge_found = True

                    concept_count = concept_count + 1

            if not merge_found:
                # check if the entity aligns with a domain term
                domain_concepts_tmp = self.get_domain_concepts(local_graph_uri, entity["text"])
                if len(domain_concepts_tmp) == 0:
                    self.add_merged_entity_to_graph(g, base_uri, entity, wikidata_entity, {}, augmented_type)
                else:
                    for dc in domain_concepts_tmp:
                        self.add_merged_entity_to_graph(g, base_uri, entity, wikidata_entity, dc, augmented_type)

                concept_count = concept_count + 1

        # add missing domain concepts to the ontology
        for domain_concept in domain_concepts:

            augmented_type = {}
            if "augmented_type" in domain_concept:
                augmented_type = domain_concept["augmented_type"]

            if "merge" in domain_concept:
                if not domain_concept["merge"]:
                    self.add_merged_entity_to_graph(g, base_uri, {}, {}, domain_concept, augmented_type)
            else:
                self.add_merged_entity_to_graph(g, base_uri, {}, {}, domain_concept, augmented_type)

            concept_count = concept_count + 1

        for augmented_type in un_mapped_augmented_types:
            self.add_merged_entity_to_graph(g, base_uri, {}, {}, {}, augmented_type)
            concept_count = concept_count + 1

        # For every variable (argument and return type), attach augmented types wherever possible
        # Augmented type links the variable to a wikidata concept, thereby semantically grounding it
        # self.add_augmented_types_to_graph(g, equations, symbols)
        # self.add_augmented_types_phrase_based(g, equations, symbols, phrases, local_graph_uri, base_uri)

        # TODO: Let's check if AutoMATES will help us here (for extraction from comments)
        # If no concepts and equations are extracted,
        # check if noun phrases align with wikidata or domain ontology
        # self.add_phrase_scientific_concept(g, phrases, local_graph_uri)

        # Update local graphs dictionary
        self.graphs_dictionary[local_graph_uri] = g

        self.save_graph(g)

        # TODO: How to send error message
        return {"numConceptsExtracted": concept_count, "numEquationsExtracted": equation_count}
    
    def add_noun_phrase_scientific_concept(self, g: Graph, phrases, local_graph_uri: str):
        np_concepts: list = []
        for phrase in phrases:
            wikidata_entities = self.el.entity_linking_for_noun_phrases(phrase)
            for w_e in wikidata_entities:
                text_str = w_e['text']
                wikidata_entity = w_e['wikidata_entity']
                np_concepts.append({"entity": {"text": text_str}, "wikidata": wikidata_entity})
        return np_concepts

    def get_domain_concepts(self, locality_uri: str, search_str: str):
        domain_concepts: list = []
        key_list: list = []
        key_list.extend(self.domain_ontologies_dictionary.keys())

        if locality_uri in key_list:
            domain_concepts = self.concept_mapper_obj.extract_uri([search_str],
                                                                  self.domain_ontologies_dictionary[locality_uri]["domain_ontology_string"])

            dg = self.domain_ontologies_dictionary[locality_uri]["graph"]

            for domain_concept in domain_concepts:
                uri_type = check_property_or_class(dg, domain_concept["uri"])
                domain_concept["uriType"] = uri_type

        return domain_concepts

    def get_domain_ontology_alignment(self, locality_uri: str, search_str: str):

        domain_align_infos = []

        domain_align_info = {}
        key_list = []
        key_list.extend(self.domain_ontologies_dictionary.keys())

        num_concepts: int = 0

        if locality_uri in key_list:
            domain_concepts = self.concept_mapper_obj.extract_uri([search_str],
                                                                  self.domain_ontologies_dictionary[locality_uri]["domain_ontology_string"])
            num_concepts = len(domain_concepts)

            dg = self.domain_ontologies_dictionary[locality_uri]["graph"]

            domain_dict = {}
            # if name is same, uri is different
            # determine if the aligned concepts is a class or property
            for domain_concept in domain_concepts:
                name = domain_concept['name']
                if name in domain_dict.keys():
                    domain_dict[name].append(domain_concept['uri'])
                else:
                    domain_dict[name] = [domain_concept['uri']]

            # triple generation
            for concept in domain_dict.keys():
                uris = domain_dict[concept]

                uris = set(uris)

                local_name = ''
                uri_info = []

                for uri in uris:
                    local_name = local_name + 'or' + dg.label(URIRef(uri))
                    uri_type = check_property_or_class(dg, uri)
                    uri_info.append({'uri': uri, 'uri_type': uri_type})

                local_name = local_name.replace("or", "", 1).strip()
                domain_align_info['localName'] = local_name
                domain_align_info['uriInfo'] = uri_info
                domain_align_info['name'] = concept
                domain_align_info['numConcepts'] = num_concepts

                domain_align_infos.append(domain_align_info)

        return domain_align_infos

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
    def add_merged_entity_to_graph(g: Graph, base_uri: str, entity, wikidata_entity, domain_concept, augmented_type):

        local_name = None

        if len(augmented_type.keys()) > 0 and 'name' in augmented_type:
            local_name = augmented_type['name']
        elif len(wikidata_entity.keys()) > 0 and 'label' in wikidata_entity:
            local_name = wikidata_entity["label"]
        elif len(domain_concept.keys()) > 0 and 'name' in domain_concept:
            local_name = domain_concept['name']

        if len(augmented_type.keys()) > 0:
            tp.populate_scientific_concept_triples(g=g, entity_uri=None,
                                                   local_name=local_name, other_label=None, base_uri=base_uri)
        if len(wikidata_entity.keys()) > 0:
            tp.populate_scientific_concept_triples(g=g, entity_uri=wikidata_entity["uri"],
                                                   local_name=local_name, other_label=entity["text"], base_uri=base_uri)
        if len(domain_concept.keys()) > 0:
            tp.add_domain_concept_triples(g=g, local_name=local_name, uri_type=domain_concept["uriType"],
                                          domain_concept=domain_concept, base_uri=base_uri)

        eq_uri = None
        if augmented_type is not None and 'eq_uri' in augmented_type:
            eq_uri = augmented_type['eq_uri']

        if eq_uri is not None:
            tp.populate_augmented_type_triples(g, eq_uri, local_name, base_uri)

        print("added to the graph")
        print(local_name, "\n", augmented_type, "\n", entity, "\n", wikidata_entity, "\n", domain_concept, "\n")

    @staticmethod
    def generate_scientific_concept_triples(g: Graph, entity, wikidata_entity, domain_align_info, base_uri):
        # triple generation
        # tp.populate_graph_entity_triples(g=g, entity_uri=wikidata_entity["uri"],
        #                                 label=wikidata_entity["label"], similarity_score=wikidata_entity["score"],
        #                                 match_text=entity["text"])

        # wikidata uri, wikidata label, all other labels, domain local name, domain uris and types

        local_name = ''

        if len(wikidata_entity.keys()) == 0:
            if 'localName' in domain_align_info:
                local_name = domain_align_info['localName']
        else:
            local_name = wikidata_entity["label"]

        local_name = urllib.parse.quote_plus(local_name)
        local_name = local_name.replace("+", "")

        if len(wikidata_entity.keys()) != 0:
            tp.populate_scientific_concept_triples(g=g, entity_uri=wikidata_entity["uri"],
                                               local_name=local_name, other_label=entity["text"], base_uri=base_uri)

        labels = []
        if 'name' in domain_align_info:
            labels.append(domain_align_info['name'])

        tp.populate_domain_info(g=g, local_name=local_name, domain_info=domain_align_info,
                                labels=labels, base_uri=base_uri)

    @staticmethod
    def extract_equation_context(entity: object, wikidata_entity: object, sent: str, symbols, line: int):
        # extract context for equation
        symbol_dict = context.eq_context_from_concept(entity, sent, line)

        if len(symbol_dict) > 0 and len(wikidata_entity) > 0:
            symbol_dict["entity_uri"] = wikidata_entity["uri"]
            if line not in symbols:
                symbol_list = [symbol_dict]
                symbols[line] = symbol_list
            else:
                symbol_list = symbols[line]
                symbol_list.append(symbol_dict)
                symbols[line] = symbol_list

    @staticmethod
    def process_scientific_equation(g: Graph, entity, equations, line: str, base_uri: str):
        equation_string = entity["text"]
        equation_parameters = t2p.text_to_python(equation_string)
        tp.populate_graph_equation_triples(g, equation_string, equation_parameters, base_uri)

        equations.append({"equation": entity["text"], "line": line, "uri": "NA",
                          "parameters": equation_parameters})

    def get_augmented_types_phease_based(self, g: Graph, equations, phrases):
        var_phrase_eq_uri_map = []

        # loop through the equations
        # get the variables.
        # get the lines and get respective chunks
        # apply the filter method

        for equation in equations:

            eq_line = equation["line"]
            start = eq_line - 2
            end = eq_line + 2

            phrase_list = []
            for i in range(start, end):
                if i in phrases:
                    phrase_list.extend(phrases[i])

            # text_equation_parameter["inputVars"] = text["inputVars"]

            eq_variables = []
            if "parameters" in equation:
                equation_list = equation["parameters"]
                for equation_info in equation_list:
                    if "text" in equation_info:
                        text_parameters = equation_info['text']
                        if "inputVars" in text_parameters:
                            eq_variables.extend((text_parameters["inputVars"]))
                        if "outputVars" in text_parameters:
                            eq_variables.extend((text_parameters["outputVars"]))

            # apply the filter method
            var_phrase_mapping = get_var_phrase_mapping(phrase_list, eq_variables)

            # this list contains variable name and it's augmented type string.
            # we need a mapping between augmented trype string and its wikidata URI

            for var_phrase_tup in var_phrase_mapping:

                # print("query for tup\n")
                # print(var_phrase_tup)
                # print()

                eq_var = var_phrase_tup[0]
                aug_type_text = var_phrase_tup[1]

                query_string = "SELECT ?uri where { ?uri <http://sadl.org/sadlimplicitmodel#localDescriptorName> \"" \
                               + eq_var + "\" }"

                query_result = g.query(query_string)
                for row in query_result:
                    var_phrase_eq_uri_map.append((eq_var, aug_type_text, row[0]))

        return var_phrase_eq_uri_map

    def add_augmented_types_phrase_based(self, g: Graph, equations, symbols, phrases, local_graph_uri, base_uri):
        # loop through the equations
        # get the variables.
        # get the lines and get respective chunks
        # apply the filter method

        for equation in equations:

            eq_line = equation["line"]
            start = eq_line - 2
            end = eq_line + 2

            phrase_list = []
            for i in range(start, end):
                if i in phrases:
                    phrase_list.extend(phrases[i])

            # text_equation_parameter["inputVars"] = text["inputVars"]

            eq_variables = []
            if "parameters" in equation:
                equation_list = equation["parameters"]
                for equation_info in equation_list:
                    if "text" in equation_info:
                        text_parameters = equation_info['text']
                        if "inputVars" in text_parameters:
                            eq_variables.extend((text_parameters["inputVars"]))
                        if "outputVars" in text_parameters:
                            eq_variables.extend((text_parameters["outputVars"]))

            # apply the filter method
            var_phrase_mapping = get_var_phrase_mapping(phrase_list, eq_variables)

            # this list contains variable name and it's augmented type string.
            # we need a mapping between augmented trype string and its wikidata URI

            for var_phrase_tup in var_phrase_mapping:

                # print("query for tup\n")
                # print(var_phrase_tup)
                # print()

                eq_var = var_phrase_tup[0]
                aug_type_text = var_phrase_tup[1]

                query_string = "SELECT ?uri where { ?uri <http://sadl.org/sadlimplicitmodel#localDescriptorName> \"" \
                               + eq_var + "\" }"

                query_result = g.query(query_string)
                for row in query_result:

                    # align aug_type_text to Wikidata and domain ontology
                    wikidata_entity = self.get_wikidata_alignment({"text": aug_type_text})
                    domain_ontology_info = self.get_domain_ontology_alignment(local_graph_uri, aug_type_text)

                    # tp.populate_graph_entity_triples(g, None, aug_type_text, 0, aug_type_text)

                    local_name = ''

                    if len(wikidata_entity.keys()) == 0:
                        if 'localName' in domain_ontology_info:
                            local_name = domain_ontology_info['localName']
                    else:
                        local_name = wikidata_entity["label"]

                    local_name = urllib.parse.quote_plus(local_name)
                    local_name = local_name.replace("+", "")

                    if len(wikidata_entity.keys()) != 0:
                        tp.populate_scientific_concept_triples(g=g, entity_uri=wikidata_entity["uri"],
                                                               local_name=local_name, other_label=aug_type_text,
                                                               base_uri=base_uri)
                    labels = []
                    if 'name' in domain_ontology_info:
                        labels.append(domain_ontology_info['name'])

                    tp.populate_domain_info(g=g, local_name=local_name, domain_info=domain_ontology_info, labels=labels,
                                            base_uri=base_uri)

                    tp.populate_augmented_type_triples(g, row[0], local_name, base_uri)

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
                        tp.populate_augmented_type_triples(g, row[0], wikidata_uri=symbol["text"],
                                                           base_uri="http://temp#")

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
            serialized_triples = str(g.serialize(format='n3').decode('utf-8'))
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

    def upload_domain_ontology(self, locality_uri: str, base_uri: str, domain_ontology_str: str):
        message = 'ontology successfully uploaded'
        dg = Graph()
        dg.parse(data=domain_ontology_str, format="application/rdf+xml")

        # TODO: Error handling if the graph can't be parsed

        # add rdfs:label to domain ontology
        dg = add_labels_to_domain_ontology(dg, "owl:Class")
        dg = add_labels_to_domain_ontology(dg, "owl:ObjectProperty")
        dg = add_labels_to_domain_ontology(dg, "owl:ObjectProperty")

        domain_ontology_str = str(dg.serialize(format='application/rdf+xml').decode('utf-8'))
        domain_ontology_str = domain_ontology_str.replace('"', '\"')

        self.domain_ontologies_dictionary[locality_uri] = {"domain_ontology_string": domain_ontology_str,
                                                           "graph": dg, "base_uri": base_uri}
        return {"message": message}


def get_eq_uri_for_var(g: Graph, var_name: str):
    eq_uris = []

    var_name_mod = pre_process(var_name)
    if len(var_name_mod) == 0:
        var_name = var_name_mod

    if var_name is not '':
        query_string = "SELECT ?uri where { ?uri <http://sadl.org/sadlimplicitmodel#localDescriptorName> \"" \
                       + var_name + "\" }"

        print("")
        print(query_string)
        print("\n")

        query_result = g.query(query_string)
        for row in query_result:
            if len(row) == 1:
                eq_uris.append(row[0])

    return set(eq_uris)


def add_labels_to_domain_ontology(dg: Graph, attr_str: str) -> Graph:
    query_string = 'select * where {?x rdf:type ' + attr_str + ' }'
    query_result = dg.query(query_string)
    for row in query_result:
        attr_uri = row[0]
        if attr_uri.rfind('#') == -1:
            attr_name = attr_uri[attr_uri.rfind('/') + 1:]
        else:
            attr_name = attr_uri[attr_uri.rfind('#') + 1:]
        attr_name = string_utils.camel_case_to_snake(attr_name, ' ')
        dg.add((URIRef(attr_uri), RDFS.label, Literal(attr_name)))
    return dg


def check_property_or_class(dg: Graph, uri: str) -> str:
    uri_type = 'class'
    query_string = 'select * where { <' + uri + '> rdf:type ?type .}'
    query_result = dg.query(query_string)
    for row in query_result:
        type_str = row[0]
        if 'property' in type_str.lower():
            uri_type = 'property'
    return uri_type


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
    print("In phrase to var mapping method ...")
    var_phrase_mapping = []
    for var in eq_var_list:
        var_tok = var.split(' ')
        var_tok_len = len(var_tok)

        for phrase in phrase_list:
            phrase = pre_process(phrase)
            phrase_mod = get_phrase_tokens(phrase, var_tok_len)
            for p_mod in phrase_mod:
                if var == p_mod:  # and ('=' not in phrase_mod)
                    print("mapping found\t" + var + "\t" + phrase)
                    # phrase_mod = phrase.replace(var, "")
                    phrase = remove_symbols(phrase)
                    if phrase_mod is not '':
                        phrase = get_phrase_concept_name(phrase, var)
                        if phrase != '':
                            print("tup saved \t" + var + "\t" + phrase)
                            var_phrase_mapping.append((var, phrase))
    return var_phrase_mapping


def get_phrase_concept_name(phrase: str, var: str):
    concept_name = ''
    phrase_tokens = phrase.split(' ')

    for phrase in phrase_tokens:
        if phrase != var:
            concept_name = concept_name + ' ' + phrase

    return concept_name.strip()


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
