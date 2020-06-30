# -*- coding: utf-8 -*-
"""
Created on Sun Feb  2 22:50:49 2020

@author: 212613144
"""
from invizin import invizin

#for visualization
import dash
import dash_core_components as dcc
import dash_html_components as html

#for communicating with services
import requests

#URL to interact with build
url_build = 'http://localhost:12345/darpa/aske/kchain/build'

#URL to interact with append service
url_append = 'http://localhost:12345/darpa/aske/kchain/append'

#URL to interact with evaluate service
url_evaluate = 'http://localhost:12345/darpa/aske/kchain/evaluate'

app = dash.Dash(
    __name__, meta_tags=[{"name": "viewport", "content": "width=device-width"}]
)

def build1():
    inputPacket = {
    "inputVariables": [
        {
            "name": "mach",
            "type": "float"
        },
        {
            "name": "gamma",
            "type": "float",
            "value":"1.4"
        }
    ],
    "outputVariables": [
        {
          "name": "aflow",
          "type": "float"
        },
        {
          "name": "fac1",
          "type": "float"
        },
        {
          "name": "fac2",
          "type": "float"
        }
    ],
    "equationModel" : """#   Utility to get the corrected airflow per area given the Mach number 
    fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))
    fac1 = tf.math.pow((1.0 + 0.5 * (gamma - 1.0) * mach * mach), fac2)
    number = 0.50161 * tf.math.sqrt(gamma) * mach / fac1
    aflow = number
    """,
    "modelName" : "getAir"
    }
    r = requests.post(url_build, json=inputPacket)
    print(r.json())


def eval1_1():
    evalPacket = {
    "inputVariables": [
        {
            "name": "mach",
            "type": "float",
            "value" : "0.9"
        },
        {
            "name": "gamma",
            "type": "float",
            "value" : "1.4"
        },
        
    ],
    "outputVariables": [
        {
          "name": "aflow",
          "type": "float"
        },
        {
          "name": "fac1",
          "type": "float"
        },
        {
          "name": "fac2",
          "type": "float"
        }
    ],
    "modelName" : "getAir"
    }
    r = requests.post(url_evaluate, json=evalPacket)
    print(r.json())
    return evalPacket

def eval1_2():
    evalPacket = {
    "inputVariables": [
        {
            "name": "mach",
            "type": "float",
            "value" : "0.9",
            "minValue": "0.0",
            "maxValue": "3.0"
        },
        {
            "name": "gamma",
            "type": "float",
            "value" : "1.4",
            "minValue": "1.01",
            "maxValue": "2.0"
        },
        
    ],
    "outputVariables": [
        {
          "name": "aflow",
          "type": "float"
        }
    ],
    "modelName" : "getAir"
    }
    r = requests.post(url_evaluate, json=evalPacket)
    print(r.json())
    return evalPacket

def eval2():
    evalPacket = {
                  "inputVariables": [
                    {
                        "name": "u0d",
                        "type": "float",
                        "value": "100.0"
                    },
                    {
                        "name": "altd",
                        "type": "float",
                        "value": "10000.0"
                    }
                  ],
                  "outputVariables": [
                    {
                      "name": "fsmach",
                      "type": "float"
                    },
                    {
                      "name": "a0",
                      "type": "float"
                    },
                    {
                      "name": "cpair",
                      "type": "float"
                    } 
                  ],
                    "modelName" : "getResponse"
                 }
    r = requests.post(url_evaluate, json=evalPacket)
    print(r.json())
    return evalPacket
    
def test1():
    build1()
    evalPacket = eval1_1()
    fig = inviz.createSensitivityGraphOAT(evalPacket)
    return fig

def test2():
    build1()
    evalPacket = eval1_2()
    fig = inviz.createSensitivityGraphOAT(evalPacket)
    return fig

def test3():
    #uses getFreeSteam0 based getResponse model
    evalPacket = eval2()
    fig = inviz.createSensitivityGraphOAT(evalPacket)
    return fig

def layout1(app, fig):
    app.layout = dcc.Graph(figure=fig)
    return app

def layout2(app, fig):
    app.layout = html.Div([
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
    return app

# Main
if __name__ == "__main__":
    inviz = invizin(url_evaluate)
    #start KCHAIN service before executing
    
    fig = test3()
    print(fig)
    app = layout2(app, fig)
    app.run_server(debug=False, port=7779)
    
    
    
    
    