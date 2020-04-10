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

# This Python file uses the following encoding: utf-8

import config
import connexion
import text_to_triples_service as t2t
import text_to_triples_backward_compatible_service as t2tbcs
import locality_search as locality
import units_extraction as units
import sys
from flask_cors import CORS

# Service Initialization. Load config file to set appropriate properties
app_text_to_triples = connexion.App(__name__, specification_dir='swagger/')
application = app_text_to_triples.app

# TODO: Error Handling if config file is not provided.
# TODO: If argv[1] is absent, use default config file from resources
application.config['config_file'] = sys.argv[1]

config = config.Config(sys.argv[1])
application.config['config_obj'] = config

# Initialize text to triples object
t2t_obj = t2t.TextToTriples(application.config['config_obj'])

# Initialize the units extraction object
units_extract = units.UnitsExtraction(application.config['config_obj'])


def text_to_triples(body):
    return t2t_obj.text_to_triples(body)


def get_equation_var_context(body):
    # return locality.get_equation_var_context(body, application.config['config_obj'])
    return locality.local_variable_search(body, t2t_obj, config)


def get_units_info(body):
    return units_extract.extract_units(body["text"], body["localityURI"], t2t_obj)


def save_graph(body):
    g = t2t_obj.get_graph(body["localityURI"])
    return t2t_obj.save_graph(g)


def clear_graph(body):
    return t2t_obj.clear_graph(body["localityURI"])


def upload_domain_ontology(body):
    return t2t_obj.upload_domain_ontology(body["localityURI"], body["baseURI"], body["ontologyAsString"])


# TODO: Backward compatible method. Need to retire. Do not use.
def text_to_triples_backward_compatible(body):
    return t2tbcs.text_to_triples_backward_compatible(body, application.config['config_obj'])


def get_equations(body):
    g = t2t_obj.get_graph(body["localityURI"])
    return t2t.get_equations(g)


def run_queries(body):
    g = t2t_obj.get_graph(body["localityURI"])
    return t2t.run_queries(g)


def process_example_doc(body):

    file_path = "demo/" + str(body["docId"]) + ".txt"
    file = open(file_path, 'r')

    content = file.readlines()
    text = ""

    for con in content:
        text = text + " " + con

    return t2tbcs.text_to_triples_backward_compatible({"text": text, "localityURI": "string"},
                                                      application.config['config_obj'])


if __name__ == '__main__':

    app_text_to_triples.add_api('text2triplesapi.yaml')
    CORS(app_text_to_triples.app)

    # print(application.config.get('config_file'))
    # app_text_to_triples.app.config['config_file'] = sys.argv[1]
    # print(app_text_to_triples.config.get('config_file'))

    app_text_to_triples.run(host='0.0.0.0', port=4200)
    app_text_to_triples.run(port=4200)
