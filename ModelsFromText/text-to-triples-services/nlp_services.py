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

import json
import requests
import re


def get_noun_chunks(nlp_service_url: str, sent: str):
    get_chunks_service_url = nlp_service_url + '/chunkSelPhraseTypePOST'

    input_info = {'phraseType': ['NP'], 'text': sent}

    headers = {'Content-Type': 'application/json'}

    input_info_json = (json.dumps(input_info))
    r = requests.post(get_chunks_service_url, input_info_json, headers=headers)
    # pprint.pprint(r.json())
    response = r.json()
    phrases = []
    for chunk_obj in response:
        if 'phrase' in chunk_obj:
            phrase_str = chunk_obj['phrase']
            phrase_tokens = re.split('and |times |on ', phrase_str)
            for p_str in phrase_tokens:
                phrases.append(p_str)
    return phrases


# TODO: Split on and, times etc.
def get_phrase_tokens(phrase_str: str):
    split_terms = get_split_terms()
    for term in split_terms:
        phrase_tokens = phrase_str.split('and')


def get_split_terms():
    return ['times', 'and']
