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
from invizin import invizin
import os
from multiprocessing import Process, active_children

#for visualization
import dash
import dash_core_components as dcc
import dash_html_components as html

#URL to interact with evaluate service
url_evaluate = 'http://localhost:12345/darpa/aske/kchain/evaluate'
# this is the host and port where the graphs will be shown
HOSTNAME = 'http://localhost:' 
GRAPH_PORT = 1177

#setting up the REST service
app = connexion.FlaskApp(__name__, specification_dir='swagger/')

dashApp = dash.Dash('dashApp', 
                    meta_tags=[{"name": "viewport", 
                                "content": "width=device-width"}])


def _deployDash(body, fig):
    global dashApp
    
    if body['plotType'] == '1':
        dashApp = _layout1(dashApp, fig)
    else:
        dashApp = _layout2(dashApp, fig)
    
    dashApp.run_server(port=GRAPH_PORT,debug=False, use_reloader=False)

    
def terminateDeploy():
    m = active_children()
    for pr in m:
        if pr.name == 'deployDash':
            print(pr)
            pr.terminate()
            print("stopped serving graphs at URL") 


def visualize(body):
    #function to create plots
    
    terminateDeploy()
        
    print(body)
    inviz = invizin(url_evaluate)
    
    fig = inviz.createSensitivityGraphOAT(body)
    
    p = Process(name = 'deployDash', target=_deployDash, 
                kwargs={"body":body, "fig":fig})
    p.start()
    print("successfully started serving graphs at URL")
    
    URL = HOSTNAME + str(GRAPH_PORT)
    
    outputPacket = {}
    outputPacket['url'] = URL
    return outputPacket


def _layout1(dashApp, fig):
    dashApp.layout = dcc.Graph(figure=fig)
    return dashApp


def _layout2(dashApp, fig):
    dashApp.layout = html.Div([
        dcc.Tabs([ #https://dash.plot.ly/dash-core-components/tabs
            dcc.Tab(label='OAT parametric', children=[
                dcc.Graph(figure=fig)
            ]),
            dcc.Tab(label='Local Gradient-based', children=[
                dcc.Graph(figure={ #https://plot.ly/python/bar-charts/#basic-bar-chart-with-plotlygraphobjects
                    'data': [
                        {'x': ['mach', 'gamma'], 'y': [2, 1],
                            'type': 'bar', 'name': 'aflow'},
                        {'x': ['mach', 'gamma'], 'y': [5, 4],
                         'type': 'bar', 'name': 'fac1'},
                        {'x': ['mach', 'gamma'], 'y': [0, 2],
                         'type': 'bar', 'name': 'fac2'},
                    ]
                })
            ]),
        ])
    ])
    return dashApp  

if __name__ == '__main__':   
    os.environ["WERKZEUG_RUN_MAIN"] = 'true'
    app.add_api('invizin_app.yaml')
    terminateDeploy()
    
    app.run(port=12309)