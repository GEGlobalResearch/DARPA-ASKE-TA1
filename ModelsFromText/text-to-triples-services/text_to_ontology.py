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

from rdflib import Graph, Namespace, RDF, RDFS, URIRef, OWL, Literal
import string_utils
import nlp_services as nlp
from segtok.segmenter import split_single


def get_ontology_from_text(body, config):
    nlp_service_url = config.NLPServiceURL
    base_uri = body['baseURI']
    paragraph = body['text']
    serialization = body['serialization']

    dict_ = {}
    units_dict_ = {}
    #sentences = nlp.get_sentences(nlp_service_url, paragraph)
    sentences = split_single(body["text"])
    for sent in sentences:
        sent = clean_up_text(sent.strip())
        dict_tup = property_class_extraction(nlp_service_url, sent, dict_, units_dict_)
    ontology_str = create_graph(dict_tup[0], dict_tup[1], base_uri, serialization)
    return {"triples": ontology_str, "serializationFormat": serialization}


def create_graph(dict_, units_dict_, base_uri, serialization):
    do = Graph()
    base = base_uri
    # base_namespace = Namespace("http://wind-turbine-auto-gen")
    # do.bind("base", base)

    for c in dict_:
        arr = dict_[c]
        c = get_valid_class_name(c)
        do.add((URIRef(base + c), RDF.type, OWL.Class))
        for a in arr:
            a_class = get_valid_class_name(a)
            a_prop = get_valid_prop_name(a)
            do.add((URIRef(base + a_prop), RDFS.domain, URIRef(base + c)))
            do.add((URIRef(base + a_prop), RDFS.range, URIRef(base + a_class)))
            do.add((URIRef(base + a_class), RDF.type, OWL.Class))

    sadl_implicit_model: Namespace = Namespace("http://sadl.org/sadlimplicitmodel#")
    for c in units_dict_:
        c_name = get_valid_class_name(c)
        do.add((URIRef(base + c_name), RDFS.subClassOf, sadl_implicit_model.UnittedQuantity))
        arr = units_dict_[c]
        for a in arr:
            do.add((URIRef(base + c_name), sadl_implicit_model.unit, Literal(a)))

    ontology_str = str(do.serialize(format=serialization).decode('utf-8'))  # application/rdf+xml | n3
    return ontology_str


def get_valid_class_name(name: str):
    name = name.replace(" ", "_")
    name = string_utils.snake_case_to_camel(name)
    if not string_utils.is_camel_case(name):
        name = name.capitalize()

    # SADL function name fix
    if name == 'sum':
        name = 'c_' + name

    first_char_ord = ord(name[0])
    if 48 <= first_char_ord <= 57:
        name = "c_" + name

    return name


def get_valid_prop_name(name: str):
    name = name.replace(" ", "_")

    # SADL function name fix
    if name == 'sum':
        name = 'p_' + name

    first_char_ord = ord(name[0])
    if 48 <= first_char_ord <= 57:
        name = "p_" + name

    return name


def ascii_clean_up(text: str):
    text = text.lower()
    count = 0
    return_text = text
    for c in text:
        val = ord(c)
        if not ((97 <= val <= 122) or (48 <= val <= 57) or val == 32):
            return_text = return_text.replace(c, "")
    return return_text


def clean_up_text(text: str):
    clean_text = text

    # removes the variable name
    tokens = text.split(' ')
    for tok in tokens:
        if tok.islower():
            clean_text = clean_text.replace(tok, '', 1)
        else:
            break
    clean_text = clean_text.lower().strip()
    rep_arr = ['@', 'return', 'returns', 'code', 'param', 'calculate', 'calculates']
    for char in rep_arr:
        clean_text = clean_text.replace(char, '')
    return clean_text


def property_class_extraction(nlp_service_url: str, text: str, dict_, units_dict_):
    # text = 'The tip speed ratio of the turbine at the blade tip.'
    # text = 'The radius of the blade in m.'
    tokens = nlp.get_tokens(nlp_service_url, text)

    tok_dict = {}
    for tok in tokens:
        tok_dict[tok['id']] = tok  # tok['token']

    # print(tok_dict)
    # get noun chunks

    phrases = nlp.get_noun_chunks_dict(nlp_service_url, text)
    # print(phrases)

    for idx in range(len(phrases)):

        phrase_obj = phrases[idx]
        p_token_list = phrase_obj['tokenIdList']
        phrase = phrase_obj['phrase']

        for id_ in p_token_list:
            pos = tok_dict[id_]['postag']
            if 'VB' in pos:
                phrase = phrase.replace(tok_dict[id_]['token'], '')

        idx_end = p_token_list[len(p_token_list) - 1]

        if (idx + 1) < len(phrases):
            phrase_obj_next = phrases[idx + 1]
            p_token_list_next = phrase_obj_next['tokenIdList']
            phrase_next = phrase_obj_next['phrase']

            for id_ in p_token_list_next:
                pos = tok_dict[id_]['postag']
                if 'VB' in pos:
                    phrase_next = phrase_next.replace(tok_dict[id_]['token'], '')

            idx_start = p_token_list_next[0]

            diff = idx_start - idx_end

            if diff == 2:
                count = idx_end + 1
                tok = ''
                while count < idx_start:
                    tok = tok + ' ' + tok_dict[count]['token']
                    count = count + 1
                if tok.strip().lower() == 'of':
                    # print(phrase.strip(), " | ", phrase_next.strip())

                    p_n = phrase_next.strip()
                    p_n = ascii_clean_up(p_n)
                    p_c = ascii_clean_up(phrase.strip())

                    if p_n is not '' and p_c is not '':
                        if p_n in dict_.keys():
                            arr = dict_[p_n]
                            if p_c not in arr:
                                arr.append(p_c)
                        else:
                            arr = [p_c]
                            dict_[p_n] = arr

                    # units extraction experiment
                    if (idx + 2) < len(phrases):
                        extract_units(phrase_obj, phrase_obj_next, phrases[idx + 2], tok_dict, units_dict_)

            elif diff == 3:
                count = idx_end + 1
                tok = ''
                while count < idx_start:
                    tok = tok + ' ' + tok_dict[count]['token']
                    count = count + 1
                if tok.strip().lower() == 'of the':
                    # print(phrase.strip(), " | ", phrase_next.strip())
                    p_n = phrase_next.strip()
                    p_n = ascii_clean_up(p_n)
                    p_c = ascii_clean_up(phrase.strip())
                    if p_n is not '' and p_c is not '':
                        if p_n in dict_.keys():
                            arr = dict_[p_n]
                            if p_c not in arr:
                                arr.append(p_c)
                        else:
                            arr = [p_c]
                            dict_[p_n] = arr
                    # units extraction experiment
                    if (idx + 2) < len(phrases):
                        extract_units(phrase_obj, phrase_obj_next, phrases[idx + 2], tok_dict, units_dict_)
    return (dict_, units_dict_)


### Experimental ###
def extract_units(phrase_obj, current_phrase_obj, next_phrase_obj, tok_dict, units_dict_):

    phrase = phrase_obj['phrase']

    p_token_list = current_phrase_obj['tokenIdList']
    idx_end = p_token_list[len(p_token_list) - 1]

    p_token_list_next = next_phrase_obj['tokenIdList']
    phrase_next = next_phrase_obj['phrase']
    idx_start = p_token_list_next[0]

    diff = idx_start - idx_end

    if diff == 2:
        count = idx_end + 1
        tok = ''
        while count < idx_start:
            tok = tok + ' ' + tok_dict[count]['token']
            count = count + 1
            if tok.strip().lower() == 'in':
                print(phrase, phrase_next)
                phrase = ascii_clean_up(phrase.strip())
                phrase_next = ascii_clean_up(phrase_next.strip())
                if phrase in units_dict_.keys():
                    arr = units_dict_[phrase]
                    if phrase_next not in arr:
                        arr.append(phrase_next)
                else:
                    arr = [phrase_next]
                    units_dict_[phrase] = arr
