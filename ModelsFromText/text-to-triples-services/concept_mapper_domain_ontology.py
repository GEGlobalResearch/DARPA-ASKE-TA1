from typing import List
import json
import requests

class ConceptMapperDomainOntology:
    nlp_service_url = ''

    def __init__(self, config):
        self.nlp_service_url =  config.NLPServiceURL

    def extract_uri(self, entity_list: List[str], domain_ontology_string: str):

        concept_mapper_service_url = self.nlp_service_url + 'conceptTaggingJSON'

        input_info = ConceptMapperDomainOntology.get_request_payload(entity_list, domain_ontology_string)
        headers = {'Content-Type': 'application/json'}
        input_info_json = (json.dumps(input_info))

        r = requests.post(concept_mapper_service_url, input_info_json, headers=headers)
        response = r.json()
        unit_info_list: List[object] = []

        if response is not None:
            print("\n")
            print(response)
            print("\n")
            for sent in response:
                concept_info_list = sent['conceptInfoList']
                for concept in concept_info_list:
                    unit_text = concept['name']
                    unit_uri = concept['uriStr']
        # parse the response
        # for unit, query and get the parent class
        # send it back

    # Creates a payload for unit extraction (aka concept tagging) request
    @staticmethod
    def get_request_payload(entity_list, domain_ontology_string):
        entity_spaced_string =  ConceptMapperDomainOntology.get_spaced_string_list(entity_list)
        paragraph_list: List[str] = [entity_spaced_string]

        owl_line_list = []
        owl_line_list[0] = domain_ontology_string

        dict_obj_list = [{'owlLineList': owl_line_list}]
        input_info = {'dictObjList': dict_obj_list, 'paragraphList': paragraph_list}
        return input_info

    @staticmethod
    def get_spaced_string_list(entity_list: List[str]):
        entity_spaced_string = ''
        for values in entity_list:
            entity_spaced_string = entity_spaced_string + " " + values

        return entity_spaced_string

    # def pre_process(self, text: str) -> str:
    #     doc = self.nlp(text)
    #     mod_text = ''
    #
    #     for token in doc:
    #         print(token.lemma_, token.text)
    #         mod_text = mod_text + " " + token.lemma_
    #
    #     return mod_text.strip()