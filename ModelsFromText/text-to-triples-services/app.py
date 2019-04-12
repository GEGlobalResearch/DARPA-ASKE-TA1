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

import connexion
import text_to_triples_service as t2t
import locality_search as locality
from flask_cors import CORS
import extract_concepts_equations as extract
import entity_linking as el
import triple_generation as tp
import equation_context as context
from segtok.segmenter import split_single


def text_to_triples(body):
    return t2t.text_to_triples(body)


def get_equation_var_context(body):
    return locality.get_equation_var_context(body)


def process_example_doc(body):

    file_path = "demo/" + str(body["docId"]) + ".txt"
    file = open(file_path, 'r')

    content = file.readlines()
    text = ""

    for con in content:
        text = text + " " + con

    return t2t.text_to_triples({"text": text, "locality": "string"})


if __name__ == '__main__':
    app = connexion.App(__name__, specification_dir='swagger/')
    app.add_api('text2triplesapi.yaml')
    CORS(app.app)
    application = app.app
    app.run(port=4200)
