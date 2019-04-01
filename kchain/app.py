# -*- coding: utf-8 -*-
"""
Created on Tue Jan 15 10:40:44 2019

@author: 212613144
"""

import connexion
import kChain as kc

if __name__ == '__main__':
    app = connexion.App(__name__, specification_dir='swagger/')
    app.add_api('my_app.yaml')
    application = app.app
    app.run(port=12345)
#    
def build(body):
    ko = kc.kChainModel(debug=True)
    
    print(body)
    
    if not 'dataLocation' in body.keys():
        body['dataLocation'] = None
    
    if not 'equationModel' in body.keys():
        body['equationModel'] = None
        
    
    ko.build(inputVar = body['inputVariables'], 
             outputVar = body['outputVariables'], 
             mdlName = body['modelName'], 
             dataLoc = body['dataLocation'],
             eqMdl = body['equationModel'])
    
    outputPacket = {"modelType" : ko.modelType,
                    "trainedState" : ko.trainedState,
                    "metagraphLocation" : ko.meta_graph_loc}
    
    print(outputPacket)
    
    return outputPacket

def evaluate(body):
    ko = kc.kChainModel(debug=True)
    
    print(body)
    
    outputVar, defaultValuesUsed, missingVar = ko.evaluate(inputVar = body['inputVariables'], 
             outputVar = body['outputVariables'], 
             mdlName = body['modelName'])
    
    outputPacket = {}
    outputPacket["outputVariables"] = outputVar
    
    if len(defaultValuesUsed) > 0:
        print('Default Values Used: ',defaultValuesUsed)
        outputPacket["defaultsUsed"] = defaultValuesUsed
    
    if missingVar != '':
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

tryLocalDemo2()
#tryLocalDemo4()