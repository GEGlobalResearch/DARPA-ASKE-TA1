import requests
import xml.etree.ElementTree as ET
import datetime
import random

entity_uri_attributes_cache = {}


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
    if "codeEquation" in parameters:
        python_code = parameters["codeEquation"]
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

    #add nil for last list_rest?

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
    triples.append(get_triple(var_uri, "<http://sadl.org/sadlimplicitmodel#descriptorName>", "\"" + var + "\""))
    triples.append(get_triple(var_uri, "<http://sadl.org/sadlimplicitmodel#dataType>",
                              "<http://sadl.org/sadlimplicitmodel#anyDataType>"))
    triples.append(get_triple(list_start, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>", var_uri))
    triples.append(get_triple(list_start, "<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>", list_rest))

    # return {"data_uri": var_uri, "triples": triples}
    return triples


def get_script_triples(eq_uri, script_uri, script_str, script_language) :
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


def get_entity_attributes(entity_uri):

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
                                      "<" + entity_type["uri"]+ ">"))
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


    #clear cache if filled up too much
    if len(entity_uri_attributes_cache) > 500:
        entity_uri_attributes_cache.clear()

    return triples


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
