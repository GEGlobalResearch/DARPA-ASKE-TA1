# -*- coding: utf-8 -*-
"""
Created on Sun Feb  2 22:09:38 2020

@author: 212613144
"""

from plotly.subplots import make_subplots
import plotly.colors as colors 
import plotly.graph_objects as go
import pandas as pd
import numpy as np
import copy
import requests


class invizin(object):
    
    def __init__(self, url_evaluate, debug=False):
        """ 
        Initialize object of type InVizIn

        Arguments:
            debug (bool): various print statements throughout the code 
                execution will be executed to help in debugging.
        """
        self.debug = debug
        self.url_evaluate = url_evaluate
        
    
    def _getOutputValue(self, evalPacket, inputVal, index):
        if inputVal is not None:
            evalPacket['inputVariables'][index]['value'] = str(inputVal)
        r = requests.post(self.url_evaluate, json=evalPacket)
        rj = r.json()
        assert r.status_code == 200
        #eval successful

        outVals = []
        for ii in range(len(rj['outputVariables'])):
            outVals.append(float(rj['outputVariables'][ii]['value'][1:-1]))
    
        return outVals
    
    def _getOutputDataframe(self, evalPacket, X, index):
        df = pd.DataFrame()
        for inputVal in X:
            outVals = self._getOutputValue(evalPacket, inputVal, index)
            dat = {}
            dat[evalPacket['inputVariables'][index]['name']] = inputVal #X
            for ix, outVal in enumerate(outVals):
                dat[evalPacket['outputVariables'][ix]['name']] = outVal
            df = df.append(dat, ignore_index=True)
            
        return df
    
    def _getMinMax(self, pck, index):
        frac = 0.1
        if 'minValue' in pck['inputVariables'][index].keys():
            MIN = float(pck['inputVariables'][index]['minValue'])
        else:
            MIN = float(pck['inputVariables'][index]['value'])*(1 - frac)
            
        if 'maxValue' in pck['inputVariables'][index].keys():
            MAX = float(pck['inputVariables'][index]['maxValue'])
        else:
            MAX = float(pck['inputVariables'][index]['value'])*(1 + frac)
        
        return MIN, MAX
    
    def createSensitivityGraphOAT(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
        NUM = 20
    
        refDat = {}
        for ii, inputVariable in enumerate(pck['inputVariables']):
            inVal = float(inputVariable['value'])
            refDat[inputVariable['name']]=inVal
    
        outVals = self._getOutputValue(evalPacket, inputVal=None, index=None)
        for ix, outVal in enumerate(outVals):
            refDat[pck['outputVariables'][ix]['name']] = outVal
    
        fig = make_subplots(rows=len(pck['outputVariables']), cols=len(pck['inputVariables']), 
                            shared_xaxes=True, horizontal_spacing=0.075, vertical_spacing=0.05) #shared_yaxes= True, shared_xaxes=True, 
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            MIN, MAX = self._getMinMax(pck, ii)
            X = np.linspace(MIN, MAX, num=NUM)
            df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
            for jj, outputVariable in enumerate(pck['outputVariables']):
                fig.add_trace(go.Scatter(x = X, y=df[outputVariable['name']], 
                                         mode="lines",name = inputVariable['name'],
                                         marker = {"size": 10}),
                              row=jj+1, col=ii+1)
                fig.add_trace(go.Scatter(x = [refDat[inputVariable['name']]], 
                                         y = [refDat[outputVariable['name']]],
                                         name = "Reference", mode="markers", 
                                         marker = {"symbol":"diamond-open","size": 10, "color": "black"}),
                              row=jj+1, col=ii+1)
                if ii == 0:
                    fig.update_yaxes(title_text=outputVariable['name'], row=jj+1, col=ii+1, hoverformat=".2f")
                else:
                    fig.update_yaxes(row=jj+1, col=ii+1, hoverformat=".2f")
                if jj == len(pck['outputVariables'])-1:
                    fig.update_xaxes(title_text=inputVariable['name'], row=jj+1, col=ii+1, hoverformat=".2f")
                else:
                    fig.update_xaxes(row=jj+1, col=ii+1, hoverformat=".2f")
    
        fig.update_layout(title='Sensitivity: One-at-a-time Parametric Analysis', showlegend=False)
        return fig