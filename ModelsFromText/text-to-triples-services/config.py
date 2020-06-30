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

import configparser


class Config:
    ElasticSearchServer = ''
    ElasticSearchIndex = ''
    NERModelFilePath = ''
    TripleStoreURL = ''
    NLPServiceURL = ''
    UnitsOntologyPath = ''
    # synonyms will be provided as 'pipe' | separated values
    UnitsSynonymURIList = ''
    UnitsOntologyGraphURI = ''
    AutomatesServiceURL = ''

    def __init__(self, config_file_path):
        config = configparser.ConfigParser()
        config.read(config_file_path)
        self.ElasticSearchServer = config["DEFAULT"]["ElasticSearchServer"]
        self.ElasticSearchIndex = config["DEFAULT"]["ElasticSearchIndex"]
        self.NERModelFilePath = config["DEFAULT"]["NERModelFilePath"]
        self.TripleStoreURL = config["DEFAULT"]["TripleStoreURL"]
        self.NLPServiceURL = config["DEFAULT"]["NLPServiceURL"]
        self.UnitsOntologyPath = config["DEFAULT"]["UnitsOntologyPath"]
        self.UnitsSynonymURIList = config["DEFAULT"]["UnitsSynonymURIList"]
        self.UnitsOntologyGraphURI = config["DEFAULT"]["UnitsOntologyGraphURI"]
        self.AutomatesServiceURL = config["DEFAULT"]["AutomatesServiceURL"]
