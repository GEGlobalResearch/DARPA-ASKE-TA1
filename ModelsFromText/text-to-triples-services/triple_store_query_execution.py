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
                    # print(binding.attrib["name"], "\t", binding[0].text)
                    response[binding.attrib["name"]] = binding[0].text
            response_list.append(response)

        return response_list
