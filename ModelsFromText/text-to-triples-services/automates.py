import requests


class Automates:
    service_url = ""

    def __init__(self, config):
        self.service_url = config.AutomatesServiceURL

    def call_service(self, file_data, entities=None):
        if entities is None:
            res = requests.post(self.service_url, headers={'Content-type': 'application/json'},
                                json={'text': file_data})
            json_response = res.json()
        else:
            res = requests.post(self.service_url, headers={'Content-type': 'application/json'},
                                json={'text': file_data, 'entities': entities})
            json_response = res.json()

        return json_response


def extract_from_json(json_string):
    extracted_relations = []
    for key in json_string:
        if key == "mentions":
            json_dic = json_string[key]
            for list_in_dict in json_dic:
                relation_map = {}
                for keys_in_list in list_in_dict:
                    if keys_in_list == "type" and list_in_dict[keys_in_list] == "RelationMention":
                        if "arguments" in list_in_dict:
                            extracted_variable = ""
                            extracted_definition = ""
                            extracted_value = ""
                            if "variable" in list_in_dict["arguments"]:
                                variable_list = list_in_dict["arguments"]["variable"]
                                for keys_in_variable_list in variable_list:
                                    extracted_variable = keys_in_variable_list["text"]
                            if "definition" in list_in_dict["arguments"]:
                                definition_list = list_in_dict["arguments"]["definition"]
                                for keys_in_definition_list in definition_list:
                                    extracted_definition = keys_in_definition_list["text"]
                            if "value" in list_in_dict["arguments"]:
                                definition_list = list_in_dict["arguments"]["value"]
                                for keys_in_definition_list in definition_list:
                                    extracted_value = keys_in_definition_list["text"]
                            if extracted_variable != "" and extracted_definition != "":
                                relation_map[extracted_variable] = extracted_definition
                                extracted_relations.append(relation_map)
                            elif extracted_variable != "" and extracted_value != "":
                                relation_map[extracted_variable] = extracted_value
                                extracted_relations.append(relation_map)
    return extracted_relations
