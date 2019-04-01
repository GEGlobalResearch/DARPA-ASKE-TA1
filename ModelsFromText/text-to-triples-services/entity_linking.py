# textdistance: https://github.com/orsinium/textdistance

import textdistance as td
import numpy as np
import requests
import xml.etree.ElementTree as ET
import json

cache = {}


def rank_candidates_elastic_search(string, candidates):
    ranked_candidates = []
    for candidate_uri in candidates:
        scored_candidate = {"uri": candidate_uri,
                            "label": candidates[candidate_uri], "score": 1.0}
        ranked_candidates.append(scored_candidate)

    return ranked_candidates


def rank_candidates(string, candidates):
    ranked_candidates = []

    for candidate_uri in candidates:
        # rank
        score = td.sorensen_dice.similarity(string, candidates[candidate_uri])
        scored_candidate = {"uri": candidate_uri, "label": candidates[candidate_uri], "score": score}
        ranked_candidates.append(scored_candidate)

    # sort
    ranked_candidates = sorted(ranked_candidates, key=lambda i: i["score"], reverse=True)
    return ranked_candidates


def get_candidates(search_str):
    candidates = {}
    query = ("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
             "select distinct ?x ?label where { " +
             "    ?subclass <http://www.wikidata.org/prop/direct/P279>* <http://www.wikidata.org/entity/Q107715> . "
             "    ?x <http://www.wikidata.org/prop/direct/P31> ?subclass . "
             "	?x rdfs:label ?label "
             "    FILTER(REGEX (?label, '" + search_str + "', 'i')). "
                                                          "    FILTER (lang(?label) = 'en') ."
                                                          "  } ")

    URL = "https://query.wikidata.org/sparql"
    PARAMS = {"query": query}

    print("\n", search_str, "\n")
    r = requests.get(url=URL, params=PARAMS, verify=False)  # proxies=proxyDict

    results = []
    try:
        root = ET.fromstring(r.text)
        if len(root) >= 1:
            results = root[1]
    except ET.ParseError:
        print(r.text)

    for res in results:
        entity_uri = res[0][0].text
        label = res[1][0].text
        candidates[entity_uri] = label
    return candidates


def get_candidates_elastic_search(search_str):
    candidates = {}
    URL = "http://localhost:9200/wikidata/_search"
    PARAMS = json.dumps(get_elastic_search_query(search_str))
    headers = {'content-type': 'application/json; charset=UTF-8'}
    r = requests.get(url=URL, data=PARAMS, headers=headers)
    results = r.json()

    hits = {}
    if "hits" in results:
        hits = results["hits"]

    matches = []
    if "hits" in hits:
        matches = hits["hits"]

    # print("\nsearch term:\t", search_str, "total results:\t", len(matches), "\n")

    for match in matches:
        match_info = match["_source"]
        if match_info["x"] not in candidates:
            candidates[match_info["x"]] = match_info["label"]
            # print(match_info["label"], match_info["x"])

    return candidates


def get_elastic_search_query(search_str):
    return {
        "query": {
            "match": {
                "label": search_str
            }
        }
    }


def get_external_matching_entity(search_str):
    # Wikidata SPARQL
    # candidates = get_candidates(search_str)
    # return one or return many? Return "top-ranked one for now"
    # scored_candidates = rank_candidates(search_str, candidates)

    # Wikidata Elastic Search
    candidates = get_candidates_elastic_search(search_str)
    scored_candidates =  rank_candidates_elastic_search(search_str, candidates)
    top_ranked_candidate = {}
    if len(scored_candidates) > 0:
        top_ranked_candidate = scored_candidates[0]

    return top_ranked_candidate
