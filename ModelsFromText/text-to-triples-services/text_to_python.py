import json
import requests


def text_to_python(string_expression):
    equation_parameters = {}
    URL = "http://localhost:5002/convertTextToCode"
    body = {"equation": string_expression}
    PARAMS = json.dumps(body)
    headers = {'content-type': 'application/json; charset=UTF-8'}
    r = requests.get(url=URL, data=PARAMS, headers=headers)

    results = r.json()

    if "inputVars" in results:
        equation_parameters["inputVars"] = results["inputVars"]
    if "returnVar" in results:
        equation_parameters["returnVar"] = results["returnVar"]
    elif "modifiedLHS" in results:
        returnVar = []
        returnVar.append(results["modifiedLHS"])
        equation_parameters["returnVar"] = returnVar
    if "codeEquation" in results:
        equation_parameters["codeEquation"] = results["codeEquation"]

    return equation_parameters
