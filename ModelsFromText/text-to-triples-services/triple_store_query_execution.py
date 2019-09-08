from typing import List
import requests
import xml.etree.ElementTree as eT


class QueryExecution:
    triple_store_url = ''

    # Initializes appropriate variables with values from the config file
    def __init__(self, config):
        self.triple_store_url = config.TripleStoreURL

    # Executes the specified query
    # Returns the response as a list of key-value pair objects
    def execute_query(self, query_string):

        # "format": "application%2Fsparql-results%2Bjson"
        params = {"query": query_string, format: "application/sparql-results/json"}

        # verify=False
        r = requests.get(url=self.triple_store_url, params=params)

        response_list: List[object] = []

        root = eT.fromstring(r.text)
        results = []
        if len(root) >= 1:
            results = root[1]

        for result in results:
            response = {}
            for binding in result:
                if len(binding) > 0:
                    print(binding.attrib["name"], "\t", binding[0].text)
                    response[binding.attrib["name"]] = binding[0].text
            response_list.append(response)

        return response_list
