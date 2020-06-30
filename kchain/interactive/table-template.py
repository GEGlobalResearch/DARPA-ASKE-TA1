# -*- coding: utf-8 -*-
"""
Created on Thu Jan  2 15:20:51 2020

@author: 212613144
"""

import dash
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd


def generate_table(dataframe, max_rows=20):
    return html.Table(
        # Header
        [html.Tr([html.Th(col[0]) for col in dataframe.columns])] +

        # Body
        [html.Tr([
            html.Td(dataframe.iloc[i][col]) for col in dataframe.columns
        ]) for i in range(min(len(dataframe), max_rows))]
    )

def deploy_table(df, app, title, max_rows):
    app.layout = html.Div(children=[
        html.H4(children=title),
        generate_table(df, max_rows)
    ])
    return app

if __name__ == '__main__':  
    #choose style
    external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']
    
    #create app object
    app = dash.Dash(__name__, external_stylesheets=external_stylesheets)
    
    #get dataset (from csv or from other functions)
    df = pd.read_csv('C:\\Users\\212613144\\Repository\\DARPA-ASKE-TA1-Ext\\Datasets\\Force_dataset.csv', header = [0,1])
    
    #layout for html page and table
    app = deploy_table(df, app, title='Force Dataset', max_rows = 10)
    
    #deploy app
    app.run_server(debug=True, use_reloader=False)