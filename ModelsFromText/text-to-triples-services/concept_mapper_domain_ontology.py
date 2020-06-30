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


class ConceptMapperDomainOntology:
    nlp_service_url = ''

    def __init__(self, config):
        self.nlp_service_url = config.NLPServiceURL

    def extract_uri(self, entity_list: List[str], domain_ontology_string: str):

        concept_mapper_service_url = self.nlp_service_url + '/conceptTaggingJSON'

        input_info = ConceptMapperDomainOntology.get_request_payload(entity_list, domain_ontology_string)
        headers = {'Content-Type': 'application/json'}
        input_info_json = (json.dumps(input_info))

        r = requests.post(concept_mapper_service_url, input_info_json, headers=headers)
        # print(r.text)
        response = r.json()
        unit_info_list: List[object] = []
        domain_concepts: List[object] = []

        if response is not None:
            # print("\nPrinting concept mapper response ...\n")
            # print(response)
            # print("\n")
            for sent in response:
                concept_info_list = sent['conceptInfoList']
                for concept in concept_info_list:
                    domain_concepts.append({'name': concept['name'], 'uri': concept['uriStr']})
        # parse the response
        # for unit, query and get the parent class
        # send it back
        else:
            print("\nNo response from concept mapper ...\n")

        return domain_concepts

    # Creates a payload for unit extraction (aka concept tagging) request
    @staticmethod
    def get_request_payload(entity_list, domain_ontology_string):
        entity_spaced_string = ConceptMapperDomainOntology.get_spaced_string_list(entity_list)
        paragraph_list: List[str] = [entity_spaced_string]

        # owl_line_list[0] = domain_ontology_string
        owl_line_list = [domain_ontology_string]

        dict_obj_list = [{'owlLineList': owl_line_list}]
        input_info = {'dictObjList': dict_obj_list, 'paragraphList': paragraph_list}
        return input_info

    @staticmethod
    def get_spaced_string_list(entity_list: List[str]):
        entity_spaced_string = ''
        for values in entity_list:
            entity_spaced_string = entity_spaced_string + ' ' + values.strip()

        return entity_spaced_string.strip()

    # def pre_process(self, text: str) -> str:
    #     doc = self.nlp(text)
    #     mod_text = ''
    #
    #     for token in doc:
    #         print(token.lemma_, token.text)
    #         mod_text = mod_text + " " + token.lemma_
    #
    #     return mod_text.strip()