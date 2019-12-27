# -*- coding: utf-8 -*-
"""
********************************************************************** 
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
 ***********************************************************************
"""

import connexion
import kChain as kc

if __name__ == '__main__':
    #setting up the REST service
    app = connexion.App(__name__, specification_dir='swagger/')
    app.add_api('kchain_app.yaml')
    #application = app.app
    app.run(port=12345)
    
def build(body):
    #wrapper function to interact with K-CHAIN build function
    
    ko = kc.kChainModel(debug=False)
    
    print(body)
    
    #handle non-required inputs with appropriate  format in JSON
    if not 'dataLocation' in body.keys():
        body['dataLocation'] = None
    
    if not 'equationModel' in body.keys():
        body['equationModel'] = None
        
    #call K-CHAIN build function with necessary general inputs
    ko.append(mdlName = None,
             inputVars = body['inputVariables'], 
             outputVars = body['outputVariables'], 
             subMdlName = body['modelName'], 
             eqMdl = body['equationModel'],
             dataLoc = body['dataLocation'])

#        ko.build(inputVar = body['inputVariables'], 
#             outputVar = body['outputVariables'], 
#             mdlName = body['modelName'], 
#             dataLoc = body['dataLocation'],
#             eqMdl = body['equationModel'])
    
    #construct output packet
    outputPacket = {"modelType" : ko.modelType,
                    "trainedState" : ko.trainedState,
                    "metagraphLocation" : ko.meta_graph_loc}
    
    print(outputPacket)
    
    return outputPacket


def append(body):
    #wrapper function to interact with K-CHAIN append function
    
    ko = kc.kChainModel(debug=True)
    
    print(body)
    
    #handle non-required inputs with appropriate format in JSON
    
    if not 'targetModelName' in body.keys():
        body['targetModelName'] = None
        
    if not 'dataLocation' in body.keys():
        body['dataLocation'] = None
    
    if not 'equationModel' in body.keys():
        body['equationModel'] = None
        
    #call K-CHAIN append function with necessary general inputs
    ko.append(mdlName = body['targetModelName'],
             inputVars = body['inputVariables'], 
             outputVars = body['outputVariables'], 
             subMdlName = body['modelName'], 
             eqMdl = body['equationModel'],
             dataLoc = body['dataLocation'])
    
    #construct output packet
    outputPacket = {"modelType" : ko.modelType,
                    "metagraphLocation" : ko.meta_graph_loc}
    
    print(outputPacket)
    
    return outputPacket

def evaluate(body):
    #wrapper function to interact with K-CHAIN evaluate function
    
    ko = kc.kChainModel(debug=False)
    
    print(body)
    
    inverseProblem = False
    
    for node in body["outputVariables"]:
        if "value" in node.keys():
            inverseProblem = True
    
    #construct output packet
    outputPacket = {}
    
    if inverseProblem:
        x_opt, fx_opt, inputVar, outputVar, defaultValuesUsed, missingVar = ko.evaluateInverse(inputVar = body['inputVariables'], 
                                                                                               outputVar = body['outputVariables'], 
                                                                                               mdlName = body['modelName'])
        outputPacket["error"] = str(fx_opt)
    else:
        #call K-CHAIN evaluate function with necessary general inputs
        outputVar, defaultValuesUsed, missingVar = ko.evaluate(inputVar = body['inputVariables'], 
                                                               outputVar = body['outputVariables'], 
                                                               mdlName = body['modelName'])
        inputVar = body['inputVariables']
        outputPacket["error"] = ''
        
        
    outputPacket["outputVariables"] = outputVar
    outputPacket["inputVariables"] = inputVar
    
    if len(defaultValuesUsed) > 0:
        #default values used in computation are sent back to inform user
        print('Default Values Used: ',defaultValuesUsed)
        outputPacket["defaultsUsed"] = defaultValuesUsed
    
    if missingVar != '':
        #missing variable information is sent back to user
        print('Need Value For: ',missingVar)
        outputPacket["missingVar"] = missingVar

    print(outputPacket)
    
    return outputPacket
    

def tryLocalDemo1():
    #Create kchain model with dataset
    inputPacket = {
                  "inputVariables": [
                    {
                      "name": "Mass",
                      "type": "double"
                    },
                    {
                      "name": "Acceleration",
                      "type": "double"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "Force",
                      "type": "double"
                    }
                  ],
                   "dataLocation" : "../Datasets/Force_dataset.csv",
                   "modelName" : "Newtons2LawModel"
                 }
    build(inputPacket)

def tryLocalDemo2():
    #Create kchain model with equation
    inputPacket = {
                  "inputVariables": [
                    {
                      "name": "Mass",
                      "type": "double",
                      "value": "1.0"
                    },
                    {
                      "name": "Acceleration",
                      "type": "double"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "Force",
                      "type": "double"
                    }
                  ],
                   "equationModel" : "Force = Mass * Acceleration",
                   "modelName" : "Newtons2LawModel"
                 }
    build(inputPacket)

def tryLocalDemo3():
    #eval kchain model with equation
    inputPacket = {
                  "inputVariables": [
                    {
                      "name": "Mass",
                      "type": "double",
                      "value": "[0.5, 0.5]"
                    },
                    {
                      "name": "Acceleration",
                      "type": "double",
                      "value": "[0.5, 0.2]"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "Force",
                      "type": "double"
                    }
                  ],
                  "modelName" : "Newtons2LawModel"
                 }
    evaluate(inputPacket)

def tryLocalDemo4():
    #eval kchain model with equation and default values
    inputPacket = {
                  "inputVariables": [
                    {
                      "name": "Acceleration",
                      "type": "double",
                      "value": "[0.5]"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "Force",
                      "type": "double"
                    }
                  ],
                  "modelName" : "Newtons2LawModel"
                 }
    evaluate(inputPacket)

def tryLocalDemo5():
    #Create kchain model with equation
    inputPacket = {
                  "inputVariables": [
                    {
                      "name": "Mass",
                      "type": "double",
                      "value": "1.0"
                    },
                    {
                      "name": "Velocity",
                      "type": "double"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "Momentum",
                      "type": "double"
                    }
                  ],
                   "equationModel" : "Momentum = Mass * Velocity",
                   "modelName" : "Momentum"
                 }
    build(inputPacket)

    
#tryLocalDemo2()
#tryLocalDemo4()