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

import dash_core_components as dcc
import dash_html_components as html

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
        self.sensitivityData = []
        
    
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
    
    def _getMinMax(self, pck, index, plotType = ''):
        frac = 0.1
        if 'minValue' in pck['inputVariables'][index].keys() and plotType != 'Relative':
            MIN = float(pck['inputVariables'][index]['minValue'])
        else:
            MIN = float(pck['inputVariables'][index]['value'])*(1 - frac)
            
        if 'maxValue' in pck['inputVariables'][index].keys() and plotType != 'Relative':
            MAX = float(pck['inputVariables'][index]['maxValue'])
        else:
            MAX = float(pck['inputVariables'][index]['value'])*(1 + frac)
        
        return MIN, MAX
    
    def _getVarName(self, var):
        if 'alias' in var.keys():
            varName = var['alias']
        else:
            varName = var['name']
        return varName
    
    def _getVarUnit(self, var):
        if 'unit' in var.keys():
            if var['unit'] != '':
                varUnit = '(' + var['unit'] + ')'
            else: 
                varUnit = ''
        else:
            varUnit = ''
        return varUnit
    
    def _getWrappedText(self, strText, w=14):
        split_text = []
        for i in range(0, len(strText), w):
            split_text.append(strText[i:i + w])
        strText = '<br>'.join(split_text)
        return strText
    
    def getOATSensitivityData(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
        NUM = 20
        sensitivityData = []
        
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            d = dict()
            d['name'] = inputVariable['name']
            d['type'] = inputVariable['type']
            d['value'] = inputVariable['value']
            
            MIN, MAX = self._getMinMax(pck, ii)
            MIN_RS, MAX_RS = self._getMinMax(pck, ii, plotType = 'Relative')
            
            if MIN != MIN_RS or MAX != MAX_RS:
                X = np.linspace(MIN, MAX, num=NUM)
                df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
                d['OATMatrix'] = df.to_dict(orient='list')
                X = np.linspace(MIN_RS, MAX_RS, num=NUM)
                df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
                d['OATRSMatrix'] = df.to_dict(orient='list')
            else:
                X = np.linspace(MIN, MAX, num=NUM)
                df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
                d['OATMatrix'] = df.to_dict(orient='list')
                d['OATRSMatrix'] = df.to_dict(orient='list')
                
            sensitivityData.append(d)    
        
        self.sensitivityData = sensitivityData
        return sensitivityData
        
    
    def createSensitivityGraphOAT(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
    
        if self.sensitivityData == []:
            #error condition
            print('Missing sensitivity data')
        else:
            sensitivityData = self.sensitivityData
            
        refDat = {}
        for inputVariable in pck['inputVariables']:
            inVal = float(inputVariable['value'])
            refDat[inputVariable['name']] = inVal
    
        outVals = self._getOutputValue(evalPacket, inputVal=None, index=None)
        for ix, outVal in enumerate(outVals):
            refDat[pck['outputVariables'][ix]['name']] = outVal
    
        fig = make_subplots(rows=len(pck['outputVariables']), 
                            cols=len(pck['inputVariables']), 
                            shared_xaxes=True, 
                            horizontal_spacing=0.075, vertical_spacing=0.05) #shared_yaxes= True 
        
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            inName = self._getVarName(inputVariable)
            inUnit = self._getVarUnit(inputVariable)
            inText = self._getWrappedText(inName)+'<br>'+inUnit
            
            X = sensitivityData[ii]['OATMatrix'][inputVariable['name']]
            
            df = sensitivityData[ii]['OATMatrix']
            for jj, outputVariable in enumerate(pck['outputVariables']):
                outName = self._getVarName(outputVariable)
                outUnit = self._getVarUnit(outputVariable)
                outText = self._getWrappedText(outName)+'<br>'+outUnit
                
                fig.add_trace(go.Scatter(x = X, y = df[outputVariable['name']], 
                                         mode="lines",name = inName,
                                         marker = {"size": 10},
                                         hoverinfo = "x+y"),
                              row=jj+1, col=ii+1)
                fig.add_trace(go.Scatter(x = [refDat[inputVariable['name']]], 
                                         y = [refDat[outputVariable['name']]],
                                         name = "Reference", mode="markers", 
                                         marker = {"symbol":"diamond-open","size": 10, "color": "black"},
                                         hoverinfo = "x+y"),
                              row=jj+1, col=ii+1)
                if ii == 0:
                    fig.update_yaxes(title_text=outText, row=jj+1, col=ii+1, hoverformat=".3f")
                else:
                    fig.update_yaxes(row=jj+1, col=ii+1, hoverformat=".3f")
                if jj == len(pck['outputVariables'])-1:
                    fig.update_xaxes(title_text=inText, row=jj+1, col=ii+1, hoverformat=".3f")
                else:
                    fig.update_xaxes(row=jj+1, col=ii+1, hoverformat=".3f")
    
        fig.update_layout(title='One-at-a-time Parametric Analysis', showlegend=False, hovermode='x')
        label = 'OAT Sensitivity Analysis'
        return fig, label
    
    def createRelativeSensitivityGraphOAT(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
        
        if self.sensitivityData == []:
            #error condition
            print('Missing sensitivity data')
        else:
            sensitivityData = self.sensitivityData
    
        refDat = {}
        for inputVariable in pck['inputVariables']:
            inVal = float(inputVariable['value'])
            refDat[inputVariable['name']]=inVal
    
        outVals = self._getOutputValue(evalPacket, inputVal=None, index=None)
        for ix, outVal in enumerate(outVals):
            refDat[pck['outputVariables'][ix]['name']] = outVal
    
        fig = make_subplots(rows=len(pck['outputVariables']), 
                            cols=1, 
                            shared_xaxes=True, 
                            vertical_spacing=0.05) #shared_yaxes= True 
        
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            inName = self._getVarName(inputVariable)
            inUnit = self._getVarUnit(inputVariable)
            inText = self._getWrappedText(inName)+'<br>'+inUnit
            
            X = sensitivityData[ii]['OATRSMatrix'][inputVariable['name']]
            df = sensitivityData[ii]['OATRSMatrix']
            
            Xd = (np.asarray(X) - refDat[inputVariable['name']])*100.0/refDat[inputVariable['name']]
            for jj, outputVariable in enumerate(pck['outputVariables']):
                outName = self._getVarName(outputVariable)
                outUnit = self._getVarUnit(outputVariable)
                outText = self._getWrappedText(outName)+'<br>'+outUnit
                
                fig.add_trace(go.Scatter(x = Xd, y=df[outputVariable['name']], 
                                         mode="lines",name = inText,
                                         marker = {"size": 10},
                                         hoverinfo = "x+y"),
                              row=jj+1, col=1)
                if ii == 0:
                    fig.add_trace(go.Scatter(x = [0.0], 
                                             y = [refDat[outputVariable['name']]],
                                             name = "Reference", mode="markers", 
                                             marker = {"symbol":"diamond-open","size": 10, "color": "black"},
                                             hoverinfo = "x+y"),
                                  row=jj+1, col=1)
                    fig.update_yaxes(title_text=outText, row=jj+1, col=1, hoverformat=".3f")

                if jj == len(pck['outputVariables'])-1:
                    fig.update_xaxes(title_text='relative change in input (%)', row=jj+1, col=1, hoverformat=".3f")
                else:
                    fig.update_xaxes(row=jj+1, col=1, hoverformat=".3f")
    
        fig.update_layout(title='One-at-a-time Parametric Analysis', showlegend=True, hovermode='x')
        label = 'Relative Sensitivity Analysis'
        return fig, label
    
    def createLocalSensitivityGraph(self, evalPacket):
        #TODO: update with gradient computation
        fig = { #https://plot.ly/python/bar-charts/#basic-bar-chart-with-plotlygraphobjects
                'data': [
                    {'x': ['mach', 'gamma'], 'y': [2, 1],
                        'type': 'bar', 'name': 'aflow'},
                    {'x': ['mach', 'gamma'], 'y': [5, 4],
                     'type': 'bar', 'name': 'fac1'},
                    {'x': ['mach', 'gamma'], 'y': [0, 2],
                     'type': 'bar', 'name': 'fac2'}
                    ]
                }
        label = 'Local Gradients'
        return fig, label
    
    def getTabLayout(self, figs, labels):
        tabs = []
        for ind, fig in enumerate(figs):
            tabs.append(dcc.Tab(label=labels[ind], 
                                children=[dcc.Graph(figure=fig)]))
    
        layout = html.Div([dcc.Tabs(tabs)])      
        return layout 

    def getSimpleLayout(self, figs):
        layout = dcc.Graph(figure=figs[0])
        return layout
    

                