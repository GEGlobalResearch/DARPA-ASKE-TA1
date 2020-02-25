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
import flask
from urllib import parse

#for visualization
import dash

#URL to interact with evaluate service
url_evaluate = 'http://localhost:12345/darpa/aske/kchain/evaluate'

# this is the port where the graphs will be shown
GRAPH_PORT = 1177

#setting up the REST service
app = connexion.FlaskApp(__name__, specification_dir='swagger/')

dashApp = dash.Dash('dashApp', 
                    meta_tags=[{"name": "viewport", 
                                "content": "width=device-width"}])

if __name__ == '__main__':   
    os.environ["WERKZEUG_RUN_MAIN"] = 'true'
    app.add_api('invizin_app.yaml')    
    app.run(port=12309)

def _deployDash(layout):
    global dashApp
    
    dashApp.layout = layout
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
    
    
    figs = []
    labels = []
    fig, label = inviz.createSensitivityGraphOAT(body)
    figs.append(fig)
    labels.append(label)
    if body['plotType'] == '1':
        layout = inviz.getSimpleLayout(figs)
    else:
        fig, label = inviz.createRelativeSensitivityGraphOAT(body)
        figs.append(fig)
        labels.append(label)
        fig, label = inviz.createLocalSensitivityGraph(body)
        figs.append(fig)
        labels.append(label)
        layout = inviz.getTabLayout(figs, labels)
    
    print(labels)
    p = Process(name = 'deployDash', target=_deployDash, 
                kwargs={"layout": layout})
    p.start()
    print("successfully started serving graphs at URL")
       
    ur = parse.urlparse(flask.request.url)
    
    URL = ur.scheme + '://' + ur.hostname + ':' + str(GRAPH_PORT)
    
    outputPacket = {}
    outputPacket['url'] = URL
    return outputPacket


