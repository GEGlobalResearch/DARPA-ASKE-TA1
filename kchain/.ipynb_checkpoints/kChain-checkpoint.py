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

"""
This module consists of kChainModel class to create, fit, append, and update 
K-CHAIN models in TensorFlow    
"""

import tensorflow as tf
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os
import json
import importlib as imp
import eqnModels
from tensorflow.python import autograph as ag

class kChainModel(object):
    
    def __init__(self, debug=False):
        """ 
        Initialize object of type K-CHAIN model.

        Arguments:
            debug (bool): various print statements throughout the code 
                execution will be executed to help in debugging.
        """
        self.debug = debug
        self.trainedState = 0
        self.modelType = ''
        self.modelGraph = tf.Graph()
        self.metagraphLoc = ''
        
    def build(self, inputVar, outputVar, mdlName, dataLoc = None, eqMdl = None):
        """
        Build a K-CHAIN model using input and output variables from the KG.

        Arguments:
            inputVar (JSON array):
                array of JSON variable objects with name (as in dataset), type, and value fields
            outputVar (JSON array):
                array of JSON variable objects with name (as in dataset), type, and value fields
            mdlName (string):
                Name to assign to the final model (E.g.: 'Newtons2ndLaw')
            dataLoc (string): 
                Location of dataset as .csv with Row 1 - Variables names, 
                Row 2 - Units, Row 3 onwards - data (default = None)
            eqMdl (string): 
                python TF eager-compatible code (e.g: "c = a * b" or "a = tf.math.sqrt(x*y)") 
                
        """
        if eqMdl is None:
            #create model with Data-driven type
            mdl, metagraphLoc = self._createNNModel(inputVar, outputVar, 
                                                    mdlName)
            self.modelType = 'NN'
            self.modelGraph = mdl
            self.meta_graph_loc = metagraphLoc
        else:
            #create model with Physics type
            mdl, metagraphLoc = self._createEqnModel(inputVar, outputVar,
                                                     mdlName, eqMdl)
            self.modelType = 'Physics'
            self.modelGraph = mdl
            self.meta_graph_loc = metagraphLoc
            
        if dataLoc is None:
            #cannot fit model without data
            self.trainedState = 0
        else:
            #fit model using dataset 
            df = self.getDataset(dataLoc)
            metagraphLoc = self.fitModel(df, inputVar, outputVar, mdlName)
            self.meta_graph_loc = metagraphLoc
            self.trainedState = 1       
        
        #get local copy of default values
        defaultValues = self._getDefaultValues()
        
        
        # add any new default values assigned in build to local copy
        for node in inputVar:
            if 'value' in node.keys():
                defaultValues[node['name']] = node['value']
        
        self._setDefaultValues(defaultValues)
        
        # Intialize the Session
        sess = tf.Session(graph=mdl)
        
        # Initialize writer to allow visualization of model in TensorBoard
        writer = tf.summary.FileWriter("log/example/model", sess.graph)
        
        # Close the writer
        writer.close()
        
        # Close the session
        sess.close()
        
        
    def getDataset(self, dataLoc = None):
        """
        Create Pandas DataFrame from identified csv.

        Arguments:
            dataLoc (string): 
                Location of dataset as .csv with Row 1 - Variables names, 
                Row 2 - Units, Row 3 onwards - data (default = None)
        
        Returns:
            df (Pandas DataFrame): DataFrame with values read from csv file 
        """
        #dataLoc = '../Datasets/Force_dataset.csv'
        df = pd.read_csv(dataLoc, header=[0,1])
        return df
    
    def _createEqnModel(self, inputVar, outputVar, mdlName, eqMdl):
        """
        Build a K-CHAIN model using input and output variables from the KG and 
        the physics equation.

        Arguments:
            inputVar (JSON array):
                array of JSON variable objects with name, type, value, and unit fields
            outputVar (JSON array):
                array of JSON variable objects with name, type, value, and unit fields
            mdlName (string):
                Name to assign to the final model (E.g.: 'Newtons2ndLaw')
            eqMdl (string):
                Equation relating inputs to output (E.g.: "c = a * b")
        
        Returns:
            (TensorFlow Graph, string):                
                * TensorFlow Graph: Computational graph of the physics equation
                * metagraphLoc: string of location on disk where computational model was stored
            
        """ 
        in_dims = len(inputVar)
                
        inStr = inputVar[0]['name'] + ' = inArg[0]'
        for ii in range(1,in_dims):
            inStr = inStr + '\n    ' + inputVar[ii]['name'] + ' = inArg['+str(ii)+']'            
        
        #4 spaces is ideal for indentation
        #construct the python function around the python snippet
        stringfun = 'import tensorflow as tf'\
        +'\ndef '+mdlName+'(inArg):'\
        +'\n    '+ inStr\
        +'\n    '+ eqMdl\
        +'\n    return '+ outputVar[0]['name'] + '\n\n'
        
        print(stringfun)
        
        #write the python code into a file where AutoGraph can read it
        self._makePyFile(stringfun)
        
        #reload the eqnModels package as a method was newly added
        imp.reload(eqnModels)
        
        #get the method created by the code
        tmp_method = getattr(eqnModels, mdlName)
        
        if self.debug:
            print(tmp_method)
        
        metagraphLoc = "../models/" + mdlName
        
        tf.reset_default_graph()
        mdl = tf.Graph()
        
        with mdl.as_default():  
            invars = []
            for ii in range(in_dims):
                #create list on input variables
                tfType = self._getVarType(inputVar[ii]['type'])
                invars.append(tf.placeholder(tfType, name=inputVar[ii]['name']))
            
            tfType = self._getVarType(outputVar[0]['type'])
            
            #create TensorFlow graph from python code
            tf_model = ag.to_graph(tmp_method)
            
            #obtain TensorFlow model of input and outputs
            output = tf_model(invars)
            
            tf.add_to_collection("output", output)
            
            for node in invars:
                tf.add_to_collection("input", node)
            
            #save model locally as a MetaGraph
            tf.train.export_meta_graph(filename = metagraphLoc+'.meta', graph = mdl)
        
        return mdl, metagraphLoc
    
    def _makePyFile(self, stringfun):
        """
        Write the formatted code into a python module for conversion to tensorflow graph
        
        Arguments:
            stringfun (string):
                formatted python code as string to be written in python file
        """
        tempPath = 'eqnModels/__init__.py'
        with open(tempPath, 'w+') as f:
            f.write(stringfun)
        #TODO: update a json array for encoded equation models with inputs, outputs, string
    
    def _getVarType(self, typeStr):
        """
        Obtain tensorflow datatypes for variable type information from KG
        
        Arguments:
            typeStr (string):
                String denoting type of variable with possible values of bool, 
                integer, float, and double (default).
        
        Returns:
             datatype in TensorFlow   (e.g. tf.bool)
        """
        if typeStr == 'integer':
            tfType = tf.int32
        elif typeStr == 'bool':
            tfType = tf.bool
        elif typeStr == 'float':
            tfType = tf.float32
        elif typeStr == 'double':
            tfType = tf.double
        else:
            if self.debug:
                print('Using default type of tf.double')
            tfType = tf.double
        return tfType

    def _createNNModel(self, inputVar, outputVar, mdlName):
        """
        Build a K-CHAIN model as a neural network using input and output 
        variables from the KG.

        Arguments:
            inputVar (JSON array):
                array of JSON variable objects with name (as in dataset) and type fields
            outputVar (JSON array):
                array of JSON variable objects with name (as in dataset) and type fields
            mdlName (string):
                Name to assign to the final model (E.g.: 'Newtons2ndLaw')
        
        Returns:
            (TensorFlow Graph, string):
                * TensorFlow Graph: computational graph of the neural network
                * metagraphLoc: string of location on disk where computational model is stored
            
        """ 
        metagraphLoc = "../models/" + mdlName
        mdl = tf.Graph()
        in_dims = len(inputVar)
        out_dims = len(outputVar)
        
        with mdl.as_default():  
            invars = []
            for ii in range(in_dims):
                #create list on input variables
                invars.append(tf.placeholder(tf.float32, name=inputVar[ii]['name']))
            
            #concatenate inputs for NN model input layer
            inputLayer = tf.concat(invars, axis=1)
            
            with tf.name_scope('NN') as scope:
                #create fixed architecture NN model
                W_0 = tf.Variable(tf.random_uniform([in_dims, 3]), name="W_0")
                b_0 = tf.Variable(tf.random_uniform([3]), name="b_0")
                h_0 = tf.nn.relu_layer(inputLayer, W_0, b_0, name="h_0")
                W_1 = tf.Variable(tf.random_uniform([3, 2]), name="W_1")
                b_1 = tf.Variable(tf.random_uniform([2]), name="b_1")
                h_1 = tf.nn.relu_layer(h_0, W_1, b_1, name="h_1")
                W_2 = tf.Variable(tf.random_uniform([2, out_dims]), name="W_2")
                b_2 = tf.Variable(tf.random_uniform([out_dims]), name="b_2")
                h_2 = tf.nn.bias_add(tf.matmul(h_1, W_2), b_2, name="h_2")
                
            output = h_2
            tf.add_to_collection("output", output)
            
            for node in invars:
                tf.add_to_collection("input", node)
            
            #save model locally as a MetaGraph
            tf.train.export_meta_graph(filename = metagraphLoc+'.meta',graph = mdl)
            return mdl, metagraphLoc
    
    def fitModel(self, dataset, inputVar, outputVar, mdlName):
        """
        Fit a K-CHAIN model using input and output variables from the KG and
        the corresponding dataset.

        Arguments:
            dataset (Pandas Dataframe):
                dataset with inputs and outputs
            inputVar (JSON array):
                array of JSON variable objects with name (as in dataset) and type fields
            outputVar (JSON array):
                array of JSON variable objects with name (as in dataset) and type fields
            mdlName (string):
                Name to assign to the final model (E.g.: 'Newtons2ndLaw')
                
        Returns:
            string: Location on disk where computational model and trained parameters are stored
            
        """ 
        in_dims = len(inputVar)
        
        # Create a clean graph and import the MetaGraphDef nodes.
        new_graph = tf.Graph()    
        
        with tf.Session(graph=new_graph) as sess:
        
            # Import the previously exported meta graph.
            new_saver = tf.train.import_meta_graph(self.meta_graph_loc+'.meta')
            
            #recollect output and input vars from original graph
            output = tf.get_collection("output")[0]

#            invars = []
#            for ii in range(in_dims):
#                invars.append(new_graph.get_tensor_by_name(inputVar[ii]+':0'))
            
            invars = tf.get_collection("input")
            
            with tf.name_scope("Training") as scope:
                #placeholder for ground truth of output
                tfType = self._getVarType(outputVar[0]['type'])
                y = tf.placeholder(tfType, [None, 1], name="y")
                
                # Define loss function for predictions w.r.t. true outputs
                loss = tf.losses.mean_squared_error(y, output)
                
                # setting up optimizer for model training
                train = tf.train.AdamOptimizer(0.001).minimize(loss, name="train_step")

            ##Define Saver
            new_saver = tf.train.Saver()
            
            #load the weights and parameter values
            
            if os.path.exists(self.meta_graph_loc+'.index'):
                if self.debug:
                    print('Load model parameters')
                new_saver.restore(sess, self.meta_graph_loc)
            else:
                if self.debug:
                    print('Initialize model parameters')
                sess.run(tf.global_variables_initializer())  
            
            ## Start the session ##		
            fd = {}
            for ii in range(in_dims):
                fd[invars[ii]] = dataset[inputVar[ii]['name']].values
            fd[y] = dataset[outputVar[0]['name']]
            
            for i in range(10000):
                sess.run(train, feed_dict=fd)
            
            metagraphLoc = "../models/" + mdlName
            new_saver.save(sess, metagraphLoc)
            
            if self.debug:
                # plot training vs predicted values
                outval = sess.run(output, feed_dict=fd)
                plt.plot(outval, '-r')
                plt.plot(dataset[outputVar[0]['name']], '-g')
                plt.show()
                
            # Close the session
            sess.close()
    
            return metagraphLoc
        
    def evaluate(self, inputVar, outputVar, mdlName):
        """
        Evaluates a model with given inputs to compute output values
        
        Arguments:
            inputVar (JSON array):
                array of JSON variable objects with name, type, value, and unit fields
            outputVar (JSON array):
                array of JSON variable objects with name, type, value, and unit fields
            mdlName (string):
                Name to model to use (E.g.: 'Newtons2ndLaw')
        
        Returns:
            (Output JSON array, Default JSON array, string):
                * Output JSON array : array of output variable JSON objects with name, type, and value fields. The resulting output of the computation is assigned to the value field of the JSON object.
                * Default JSON array : array of default variable JSON objects with name and value fields. 
                * string : Name of missing variable, which is needed for inference
            
        """
        metagraphLoc = "../models/" + mdlName
        
        # Create a clean graph and import the MetaGraphDef nodes.
        new_graph = tf.Graph()    
        
        with tf.Session(graph=new_graph) as sess:
        
            # Import the previously exported meta graph.
            new_saver = tf.train.import_meta_graph(metagraphLoc+'.meta')
            
            #load the weights and parameter values
            try: 
                new_saver.restore(sess, metagraphLoc)
            except ValueError:
                ## Initialize values in the session ##
                sess.run(tf.global_variables_initializer())
            except AttributeError:
                print('AttributeError showed up')
                
            #recollect output and input vars from original graph
            output = tf.get_collection("output")[0]    
            
            #setup feed dictionary for task at hand
            fd = {}
            #assign values to nodes
            for node in inputVar:
                if '[' in node['value'] and ']' in node['value']:
                    tstr = node['value'][1:-1]
                else:
                    tstr = node['value']
                fd[new_graph.get_tensor_by_name(node['name']+':0')] = np.fromstring(tstr, 
                   sep = ',').reshape(-1,1)
        
            #get output predictions
            #outval = sess.run(output, feed_dict=fd)
            defaultValues = self._getDefaultValues()
            defaultValuesUsed = []
            outval, defaultValuesUsed, missingVar = self._runSessionWithDefaults(sess, new_graph, inputVar,
                                                                     output, fd, defaultValues, 
                                                                     defaultValuesUsed)
            # Close the session
            sess.close()
        
        if outval is not None:
            outputVar[0]['value'] = np.array2string(outval.reshape(-1,),
                                                     separator = ',')
        else:
            outputVar[0]['value'] = None
                
        return outputVar, defaultValuesUsed, missingVar
    
    def _runSessionWithDefaults(self, sess, mdl, inputVar, output, fd, defaultValues, defaultValuesUsed):
        """
        Run a TensorFlow session with given inputs and fill in missing values from defaults or inform user about missing information
        
        Arguments:
            sess (TF Session):
                TensorFlow session to run the computation
            mdl (TF Graph):
                TensorFlow Graph to run the session
            inputVar (JSON array):
                array of JSON variable objects with name, type, and value fields
            output (TF Graph Node):
                TensorFlow Graph node for output from computation
            feed_dict (Dictionary):
                Feed dictionary for TF placeholders and their values
            defaultValues (JSON):
                Name:Value pairs of default values from model build stage
            defaultValuesUsed (JSON Array):
                array of default variable JSON objects with name and value fields (empty initially)
    
        Returns:
            (output type, Default JSON array, string):
                * output: value(s) of output variable.
                * Default JSON array : array of default variable JSON objects with name and value fields. 
                * string : Name of missing variable, which is needed for inference
            
        """
        missingVar = ''
        try:
            #if all required inputs are available in correct type then no InvalidArgumentError
            aval = sess.run(output, feed_dict=fd)
        except tf.errors.InvalidArgumentError as e:
            #determine if argument error was raised by placeholder or operation 
            if e.node_def.op != 'Placeholder':
                # This case is usually not required
                # identify input of operation that is missing
                tmp = 0
                ii = 0
                varName = '' 
                while varName == '' and ii < len(e.node_def.input):
                    for node in inputVar:
                        if node['name'] == e.node_def.input[ii]:
                            tmp = 1
                    if tmp == 0:
                        varName = e.node_def.input[ii]
                    else:
                        ii = ii + 1
            else:
                #if placeholder is missing, then  return name of placeholder
                varName = e.node_def.name 
                
            #look for value available in default values
            if varName in defaultValues.keys():
                tmp = mdl.get_tensor_by_name(varName+':0')
                #if user wants multiple evaluations together, the default values
                #need to be repeated same number of times.
                if len(fd) > 0: 
                    #Unpacking with * works with any object that is iterable and, 
                    #since dictionaries return their keys when iterated through, 
                    #* creates a list of keys by using it within a list literal
                    fd[tmp] = np.repeat(np.fromstring(defaultValues[varName], 
                      dtype = float, sep = ',').reshape(-1,1), len(fd[[*fd][0]]), axis=0)
                else:
                    fd[tmp] = np.fromstring(defaultValues[varName], 
                      dtype = float, sep = ',').reshape(-1,1)
                    
                defaultValuesUsed.append({'name':varName,'value':defaultValues[varName]})
                #run session with updated list of inputs from default values
                aval, defaultValuesUsed, missingVar = self._runSessionWithDefaults(sess, mdl, inputVar, 
                                                                                   output, fd, defaultValues, 
                                                                                   defaultValuesUsed)
            else:
                #if missing variable is not in the list of default variables, then inform user
                print('Please provide value for : ' + varName)
                aval = None
                missingVar = varName
                
        return aval, defaultValuesUsed, missingVar
    
    def _getDefaultValues(self):
        """
        Reads json from file and return if exists, else create new and return empty 
        """
        try:
            with open('defaultValues.txt', 'r') as json_file:  
                defaultValues = json.load(json_file)
        except IOError:
            defaultValues = {}
        return defaultValues
    
    
    def _setDefaultValues(self, defValues):
        """
        Writes json with provided values back to file
        """
        with open('defaultValues.txt', 'w') as outfile:  
            json.dump(defValues, outfile, indent=4)
        