import extract_concepts_equations as extract
import entity_linking as el
import triple_generation as tp
import equation_context as context
import text_to_python as t2p
from segtok.segmenter import split_single
import random


def get_replace_chars():
    return ['.', '[', ']', '(', ')', ',', ';', ':', '!', "?", "\""]


def pre_process(string):
    replace = get_replace_chars()
    for char in replace:
        string = string.replace(char, "")
    return string.strip()


def text_to_triples(body):
    text_triples_arr = []

    # Run the NER and get extracted concepts
    # Load the NER model in memory
    model_file_path = "../resources/taggers/ner-equations-01-23-2019/final-model.pt"
    model = extract.load_model(model_file_path)

    line = 1
    symbols = []
    equations = []
    text_triple_dict = {}
    page_cache = {}

    # Break paragraphs into setences
    sentences = split_single(body["text"])
    for sent in sentences:
        sent = sent.replace("\n", " ")
        sent = sent.strip()

        entity_dict = {}

        if sent != "":
            entity_dict = extract.extract(model, sent)
            # print(entity_dict, "\n")

        # Entity Linking
        entities = {}
        concepts = []
        triples = []

        if len(entity_dict) > 0:
            entities = entity_dict["entities"]

        for entity in entities:

            if entity["type"] == "CONCEPT":
                # take label and call get_external_matching_entity

                search_str = entity["text"]
                search_str = pre_process(search_str)
                top_match_entity = {}

                if search_str.lower() in page_cache:
                    top_match_entity = page_cache[search_str.lower()]
                else:
                    top_match_entity = el.get_external_matching_entity(search_str)
                    page_cache[search_str.lower()] = top_match_entity

                # print(top_match_entity, "\n")
                entity_uri = ""
                if len(top_match_entity) == 3:
                    entity_uri = top_match_entity["uri"]
                    triples = tp.generate_entity_triples(top_match_entity["uri"], top_match_entity["label"],
                                                         top_match_entity["score"], entity["text"])

                # extract context for equation
                symbol_dict = context.eq_context_from_concept(entity, sent, line)
                if len(symbol_dict) > 0:
                    symbol_dict["entity_uri"] = entity_uri
                    symbols.append(symbol_dict)

            else:
                equation_string = entity["text"]
                score = entity["confidence"]
                equation_parameters = t2p.text_to_python(equation_string)
                triples = tp.generate_equation_triples(equation_string, equation_parameters)
                equations.append({"equation": entity["text"], "line": line,"uri": "NA",
                                  "parameters": equation_parameters})

                # triples = tp.generate_equation_triples(equation_string)
                # equation_info = tp.generate_equation_triples(equation_string)

                # for context
                # if len(equation_info) == 2:
                #    equation_parameters = t2p.text_to_python(equation_string)
                #   triples = equation_info["triples"]
                #    equations.append({"equation": entity["text"], "line": line,
                #                      "uri": equation_info["uri"], "parameters": equation_parameters})

                # we will get the variable info here from Jobin's REST calls

            # generate triples for the extracted entity
            concept = {"string": entity["text"], "extractionConfScore": entity["confidence"],
                       "start": entity["start_pos"],
                       "end": entity["end_pos"], "type": entity["type"], "triples": triples}
            concepts.append(concept)
            # concept_dict = {"line": (line-1), "concept": concept}
            # concepts.append(concept_dict)

        # triple gen
        # what triples do you want to gen.?
        # rdfs:label, type (prop. v/s class), other properties availalble to query
        if sent != "":
            text_triples = {"text": sent, "concepts": concepts}
            text_triple_dict[line] = text_triples
            # text_triples_arr.append(text_triples)

        line = line + 1

    print("\n", equations, "\n")
    for line in text_triple_dict:
        text_triples = text_triple_dict[line]
        for eq in equations:
            if eq["line"] == line:
                for symbol in symbols:
                    diff = symbol["line"] - eq["line"]
                    if abs(diff) <= 2 and (symbol["symbol"] in eq["equation"]):
                        concepts = text_triples["concepts"]
                        for concept in concepts:
                            if concept["string"] == eq["equation"]:
                                # to do the extension we need the data desc. URI
                                data_desc_uri = context.get_data_descriptor_uri(symbol["symbol"], concept["triples"])
                                if data_desc_uri != "NA":
                                    concept["triples"].extend(
                                        tp.get_equation_context_triples(data_desc_uri=data_desc_uri,
                                                                wikidata_uri=symbol["entity_uri"], rand=random.random()))
        text_triples_arr.append(text_triples)


    # print("\n", equations, "\n", symbols, "\n")
    # sep. equation and concepts while looping?? (for appending togther in next for loop
    # process speed of sound; upload; locality search URI
    #for eq in equations:
        #text_triples = text_triple_dict[eq["line"]]
        #for symbol in symbols:
        #    diff = symbol["line"] - eq["line"]
        #    if abs(diff) <= 2:
        #       if symbol["symbol"] in eq["equation"]:
        #            # print('\n')
        #           concepts = text_triples["concepts"]
        #           for concept in concepts:
        #               if concept["string"] == eq["equation"]:
        #                   # print(eq, symbol)
        #                   # concept["triples"].append({"subject": "hello", "predicate": "word", "object": symbol["entity_uri"]})
        #                   concept["triples"].extend(
        #                       tp.get_equation_context_triples(symbol["symbol"], symbol["entity_uri"],
        #                                                       eq["uri"], eq["parameters"])
        #                   )
      #  text_triples_arr.append(text_triples)

    #for eq in equations:
    #    text_triple_dict.pop(eq["line"])

    #for line in text_triple_dict:
    #   text_triples_arr.append(text_triple_dict[line])

    with open('triples.nt', 'w') as f:
        for tt in text_triples_arr:
            concepts = tt["concepts"]
            for concept in concepts:
                triples = concept["triples"]
                for triple in triples:
                    obj_str = triple["object"]
                    if (obj_str.startswith("<") == False) and (obj_str.startswith("_:") == False):
                        obj_str = "\"" + obj_str + "\""
                    line = triple["subject"] + " " + triple["predicate"] + " " + obj_str + " .\n"
                    f.write(line)

    return text_triples_arr
