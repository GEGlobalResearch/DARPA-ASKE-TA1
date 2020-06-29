"""
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
"""


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
