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
        self.OATSensitivityData = []
        self.normalizedSensitivityData = []
        
    
    def _getOutputValue(self, evalPacket, inputVal, index):
        """Compute an output using a model evaluation service 
    
        Using KCHAIN evaluation service, to get an output for a given input value 
        inputVal, where JSON for evaluation service call is evalPacket.
        
    
        Args:
            evalPacket (JSON): JSON packet for call to evaluation service
            inputVal (float): value of input
            index (int): index of input variable to be modified
    
        Returns:
            list:  
    
        """
        if inputVal is not None:
            evalPacket['inputVariables'][index]['value'] = str(inputVal)
        r = requests.post(self.url_evaluate, json=evalPacket)
        rj = r.json()
        assert r.status_code == 200
        #eval successful
        
        outVals = []
        for ii in range(len(rj['outputVariables'])):
            if '[' in rj['outputVariables'][ii]['value']:
                outVals.append(float(rj['outputVariables'][ii]['value'][1:-1]))
            else: 
                outVals.append(float(rj['outputVariables'][ii]['value']))
    
        return outVals
    
    def _getOutputDataframe(self, evalPacket, X, index):
        df = pd.DataFrame()
        
        if evalPacket['CGType'] != "python":
            for inputVal in X:
                outVals = self._getOutputValue(evalPacket, inputVal, index)
                dat = {}
                dat[evalPacket['inputVariables'][index]['name']] = inputVal #X
                for ix, outVal in enumerate(outVals):
                    dat[evalPacket['outputVariables'][ix]['name']] = outVal
                df = df.append(dat, ignore_index=True)
        else:
            evalPacket['inputVariables'][index]['value'] = str(X.tolist())
            r = requests.post(self.url_evaluate, json=evalPacket)
            rj = r.json()
            assert r.status_code == 200
            #eval successful
            for outputVariable in rj['outputVariables']:
                if '[' in outputVariable['value'] and ']' in outputVariable['value']:
                    tstr = outputVariable['value'][1:-1]
                else:
                    tstr = outputVariable['value']
                
                df[outputVariable['name']] = pd.Series(np.fromstring(tstr, sep = ','))
            
            vName = self._getVarName(var = evalPacket['inputVariables'][index], 
                                     aliasFlag = False)
            df[vName] = pd.Series(X)
            
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
    
    def _getVarName(self, var, aliasFlag = True):
        if 'alias' in var.keys() and aliasFlag:
            varName = var['alias']
        else:
            v = var['name']
            ind = v.find('_val')
            if ind != -1 and v.endswith('_val'):
                varName = v[0:ind]
            else:
                varName = v
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
    
    def _getWrappedText(self, strText, w=25):
        split_text = []
        for i in range(0, len(strText), w):
            split_text.append(strText[i:i + w])
        strText = '<br>'.join(split_text)
        return strText
    
    def getOATSensitivityData(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
        NUM = 21
        OATSensitivityData = []
        
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            d = dict()
            d['name'] = self._getVarName(var = inputVariable, aliasFlag = False)
            d['type'] = inputVariable['type']
            d['value'] = inputVariable['value']
            xq = float(d['value'])
            
            MIN, MAX = self._getMinMax(pck, ii)
            MIN_RS, MAX_RS = self._getMinMax(pck, ii, plotType = 'Relative')
            
            if MIN != MIN_RS or MAX != MAX_RS:
                #X = np.linspace(MIN, MAX, num=NUM)
                X = np.concatenate((np.linspace(MIN, xq, num=int(np.floor(NUM/2)), endpoint=False), 
                                    np.linspace(xq, MAX, num=int(np.ceil(NUM/2)))))
                df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
                d['OATMatrix'] = df.to_dict(orient='list')
                XRS = np.linspace(MIN_RS, MAX_RS, num=NUM)
                df = self._getOutputDataframe(copy.deepcopy(pck), XRS, index = ii)
                d['OATRSMatrix'] = df.to_dict(orient='list')
            else:
                X = np.linspace(MIN, MAX, num=NUM)
                df = self._getOutputDataframe(copy.deepcopy(pck), X, index = ii)
                d['OATMatrix'] = df.to_dict(orient='list')
                d['OATRSMatrix'] = df.to_dict(orient='list')
                
            OATSensitivityData.append(d)    
        
        self.OATSensitivityData = OATSensitivityData
        return OATSensitivityData
    
    def getJacobian(self, evalPacket):
        r = requests.post(self.url_evaluate+'Grad', json=evalPacket)
        rj = r.json()
        print(rj)
        
        self.normalizedSensitivityData = rj['jacobian']
        return rj['jacobian'], rj['errorMessage']
    
    def createOATSensitivityGraph(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
    
        if self.OATSensitivityData == []:
            #error condition
            print('Missing sensitivity data')
        else:
            sensitivityData = self.OATSensitivityData
            
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
            
            X = sensitivityData[ii]['OATMatrix'][self._getVarName(inputVariable, aliasFlag=False)]
            
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
                    fig.update_yaxes(title_text=outText, row=jj+1, col=ii+1, 
                                     hoverformat=".3f", titlefont=dict(size=12))
                else:
                    fig.update_yaxes(row=jj+1, col=ii+1, hoverformat=".3f")
                if jj == len(pck['outputVariables'])-1:
                    fig.update_xaxes(title_text=inText, titlefont=dict(size=12),
                                     row=jj+1, col=ii+1, hoverformat=".3f")
                else:
                    fig.update_xaxes(row=jj+1, col=ii+1, hoverformat=".3f")
    
        fig.update_layout(title='One-at-a-time Parametric Analysis', 
                          showlegend=False, hovermode='x',
                          autosize=True, height=200*(1+len(pck['outputVariables'])))
        label = 'OAT Parametric Analysis'
        return fig, label
    
    def createOATRelativeSensitivityGraph(self, evalPacket):
        pck = copy.deepcopy(evalPacket)
        
        if self.OATSensitivityData == []:
            #error condition
            print('Missing sensitivity data')
        else:
            sensitivityData = self.OATSensitivityData
    
        refDat = {}
        for inputVariable in pck['inputVariables']:
            inVal = float(inputVariable['value'])
            refDat[inputVariable['name']]=inVal
    
        outVals = self._getOutputValue(evalPacket, inputVal=None, index=None)
        for ix, outVal in enumerate(outVals):
            refDat[pck['outputVariables'][ix]['name']] = outVal
    
        cols = colors.DEFAULT_PLOTLY_COLORS
        
        fig = make_subplots(rows=len(pck['outputVariables']), 
                            cols=1, 
                            shared_xaxes=True, 
                            vertical_spacing=0.05) #shared_yaxes= True 
        
        for ii, inputVariable in enumerate(pck['inputVariables']):
            #only one input is changed while others stay at base
            inName = self._getVarName(inputVariable)
            inUnit = self._getVarUnit(inputVariable)
            inText = self._getWrappedText(inName)+'<br>'+inUnit
            
            X = sensitivityData[ii]['OATRSMatrix'][self._getVarName(inputVariable, aliasFlag=False)]
            df = sensitivityData[ii]['OATRSMatrix']
            
            Xd = (np.asarray(X) - refDat[inputVariable['name']])*100.0/refDat[inputVariable['name']]
            for jj, outputVariable in enumerate(pck['outputVariables']):
                outName = self._getVarName(outputVariable)
                outUnit = self._getVarUnit(outputVariable)
                outText = self._getWrappedText(outName)+'<br>'+outUnit
                
                if jj == 0:
                    fig.add_trace(go.Scatter(x = Xd, y=df[outputVariable['name']], 
                                             mode="lines",name = inText,
                                             marker = {"size": 10},
                                             line_color = cols[ii],
                                             hoverinfo = "x+y"),
                                  row=jj+1, col=1)
                else:
                    fig.add_trace(go.Scatter(x = Xd, y=df[outputVariable['name']], 
                                             mode="lines",name = inText,
                                             marker = {"size": 10},
                                             line_color = cols[ii],
                                             showlegend = False,
                                             hoverinfo = "x+y"),
                                  row=jj+1, col=1)
                    
                if ii == 0:
                    if jj == 0:
                        fig.add_trace(go.Scatter(x = [0.0], 
                                                 y = [refDat[outputVariable['name']]],
                                                 name = "Reference", mode="markers", 
                                                 marker = {"symbol":"diamond-open","size": 10, "color": "black"},
                                                 hoverinfo = "x+y"),
                                      row=jj+1, col=1)
                    else:
                        fig.add_trace(go.Scatter(x = [0.0], 
                                                 y = [refDat[outputVariable['name']]],
                                                 name = "Reference", mode="markers", 
                                                 marker = {"symbol":"diamond-open","size": 10, "color": "black"},
                                                 hoverinfo = "x+y",
                                                 showlegend = False),
                                      row=jj+1, col=1)
                            
                    fig.update_yaxes(title_text=outText, row=jj+1, col=1, 
                                     hoverformat=".3f", titlefont=dict(size=12))

                if jj == len(pck['outputVariables'])-1:
                    fig.update_xaxes(title_text='relative change in input (%)', 
                                     row=jj+1, col=1, hoverformat=".3f", titlefont=dict(size=12))
                else:
                    fig.update_xaxes(row=jj+1, col=1, hoverformat=".3f")
    
        fig.update_layout(title='One-at-a-time Relative Parametric Analysis', 
                          showlegend=True, hovermode='x',
                          autosize=True, height=200*(1+len(pck['outputVariables'])))
        label = 'Relative Sensitivity Analysis'
        return fig, label
    
    def createNormalizedSensitivityGraph(self, evalPacket):
        
        if self.normalizedSensitivityData == []:
            #error condition
            print('Missing sensitivity data')
        else:
            J = self.normalizedSensitivityData
            
        plotData = []
        inpNames = []
        for inputVar in iter(evalPacket['inputVariables']):
            inpNames.append(self._getVarName(inputVar))
            
        for ind, outputVar in enumerate(evalPacket['outputVariables']):
            plotData.append(go.Bar(name=self._getVarName(outputVar), x=inpNames, y=J[ind]))
        
        fig = go.Figure(data = plotData)
        # Change the bar mode
        fig.update_yaxes(hoverformat=".3f", titlefont=dict(size=12))
        fig.update_layout(barmode='group', 
                          xaxis_tickangle=-45,
                          yaxis_title = 'Sensitivity',
                          hovermode = 'x',
                          title = 'Normalized Gradients')
        
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
    

                