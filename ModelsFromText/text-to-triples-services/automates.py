import requests


class ServiceCalls:
    service_url = ""

    def __init__(self, config):
        self.service_url = config.ServiceURL

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
