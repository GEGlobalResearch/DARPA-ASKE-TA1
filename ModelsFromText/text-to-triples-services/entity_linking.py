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

# textdistance: https://github.com/orsinium/textdistance


import textdistance as td
import requests
import xml.etree.ElementTree as ET
import json

cache = {}


class EntityLinking:
    elastic_search_server = ''
    elastic_search_index = ''

    def __init__(self, config):
        self.elastic_search_server = config.ElasticSearchServer
        self.elastic_search_index = config.ElasticSearchIndex

    @staticmethod
    def rank_candidates_elastic_search(self, candidates):
        sorted_candidates = {k: v for k, v in sorted(candidates.items(), key=lambda item: item[1][0], reverse=True)}
        ranked_candidates = []
        for candidate_uri in sorted_candidates:
            scored_candidate = {"uri": candidate_uri,
                            "label": candidates[candidate_uri][1], "score": candidates[candidate_uri][0]}
            ranked_candidates.append(scored_candidate)

        return ranked_candidates

    def rank_candidates(self, string, candidates):
        ranked_candidates = []

        for candidate_uri in candidates:
            # rank
            score = td.sorensen_dice.similarity(string, candidates[candidate_uri])
            scored_candidate = {"uri": candidate_uri, "label": candidates[candidate_uri], "score": score}
            ranked_candidates.append(scored_candidate)

        # sort
        ranked_candidates = sorted(ranked_candidates, key=lambda i: i["score"], reverse=True)
        return ranked_candidates

    def get_candidates(self, search_str):
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

    def get_candidates_elastic_search(self, search_str):
        candidates = {}
        URL = self.elastic_search_server + "/" + self.elastic_search_index + "/_search"
        PARAMS = json.dumps(self.get_elastic_search_query(search_str))

        headers = {'content-type': 'application/json; charset=UTF-8'}
        r = requests.get(url=URL, data=PARAMS, headers=headers)
        results = r.json()

        hits = {}
        if "hits" in results:
            hits = results["hits"]

        matches = []
        if "hits" in hits:
            matches = hits["hits"]

        for match in matches:
            match_info = match["_source"]
            if match_info["wikidata_link"] not in candidates:
                # candidates[match_info["x"]] = match_info["label"]
                soren_dice_score = td.sorensen_dice(search_str, match_info["label"])
                candidates[match_info["wikidata_link"]] = [soren_dice_score, match_info["label"]]

        return candidates

    def get_elastic_search_query(self, search_str):
        return {
            "query": {
                "match": {
                    "label": search_str
                }
            }
        }

    def get_external_matching_entity(self, search_str):
        # Wikidata SPARQL
        # candidates = get_candidates(search_str)
        # return one or return many? Return "top-ranked one for now"
        # scored_candidates = rank_candidates(search_str, candidates)

        # Wikidata Elastic Search
        candidates = self.get_candidates_elastic_search(search_str)
        scored_candidates = self.rank_candidates_elastic_search(search_str, candidates)
        top_ranked_candidate = {}
        if len(scored_candidates) > 0:
            top_ranked_candidate = scored_candidates[0]

            if 'label' in top_ranked_candidate:
                try:
                    float(top_ranked_candidate['label'])
                    top_ranked_candidate = {}
                except ValueError:
                    is_number = False

            if "score" in top_ranked_candidate:
                score = top_ranked_candidate["score"]
                if score < 0.9:
                    top_ranked_candidate = {}

        return top_ranked_candidate

    def entity_linking_for_noun_phrases(self, search_str):
        wikidata_entities: list = []
        top_ranked_candidate = self.get_external_matching_entity(search_str)
        if len(top_ranked_candidate) == 0:
            tokens = search_str.strip().split(' ')
            if len(tokens) > 1:
                for token in tokens:
                    wikidata_entity = self.get_external_matching_entity(token)
                    if len(wikidata_entity) > 0:
                        wikidata_entities.append({"text": token, "wikidata_entity": wikidata_entity})
        else:
            wikidata_entities.append({"text": search_str, "wikidata_entity": top_ranked_candidate})

        return wikidata_entities

