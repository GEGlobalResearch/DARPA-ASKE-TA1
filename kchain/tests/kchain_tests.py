# -*- coding: utf-8 -*-
"""
Created on Wed Mar  4 15:34:01 2020

@author: 212613144
"""

buildPacket1 = \
{
  "inputVariables": [
    {
      "name": "mach",
      "alias": "MachNumber",
      "type": "float"
    },
    {
      "name": "gamma",
      "type": "float"
    }
  ],
  "outputVariables": [
    {
      "name": "aflow",
      "alias": "Airflow",
      "type": "float",
      "unit": "lbs/ft^2"
    }
  ],
  "modelName": "getAirPy",
  "CGType": "python",
  "equationModel": "#Utility to get the corrected airflow per area given the Mach number\n    fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))\n    fac1 = np.power((1.0 + 0.5 * (gamma - 1.0) * mach * mach), fac2)\n    number = 0.50161 * np.sqrt(gamma) * mach / fac1\n    aflow = number"
}


evalPacket1 = \
{
  "inputVariables": [
    {
      "name": "mach",
      "alias": "MachNumber",
      "type": "float",
      "value": "0.9",
      "minValue": "0.0",
      "maxValue": "3.0"
    },
    {
      "name": "gamma",
      "type": "float",
      "value": "1.4",
      "minValue": "1.01",
      "maxValue": "2.0"
    }
  ],
  "outputVariables": [
    {
      "name": "aflow",
      "alias": "Airflow",
      "type": "float",
      "unit": "lbs/ft^2"
    }
  ],
  "modelName": "getAirPy",
  "CGType" : "python"
}

evalPacket2 = \
{
  "inputVariables": [
    {
      "name": "mach",
      "alias": "MachNumber",
      "type": "float",
      "value": "0.9",
      "minValue": "0.0",
      "maxValue": "3.0"
    },
    {
      "name": "gamma",
      "type": "float",
      "value": "[1.01, 1.12, 1.23, 1.34, 1.45, 1.56, 1.67, 1.78, 1.8900000000000001, 2.0]",
      "minValue": "1.01",
      "maxValue": "2.0"
    }
  ],
  "outputVariables": [
    {
      "name": "aflow",
      "alias": "Airflow",
      "type": "float",
      "unit": "lbs/ft^2"
    }
  ],
  "modelName": "getAirPy",
  "CGType" : "python"
}