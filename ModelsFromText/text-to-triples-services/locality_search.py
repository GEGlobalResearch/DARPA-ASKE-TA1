import requests
import xml.etree.ElementTree as ET


def get_equation_var_context(body):
    equation_context = []

    query_string = get_query_string(body["variableName"], body["localityURI"])

    URL = "http://vesuvius-test.crd.ge.com:2420/sparql"
    PARAMS = {"query": query_string, format: "application/sparql-results/json"} #"format": "application%2Fsparql-results%2Bjson"

    r = requests.get(url=URL, params=PARAMS) #verify=False
    print(r.text, "\n")
    root = ET.fromstring(r.text)
    results = []
    if len(root) >= 1:
        results = root[1]

    for result in results:
        context = {}
        for binding in result:
            if len(binding) > 0:
                print(binding.attrib["name"], "\t", binding[0].text)
                context[binding.attrib["name"]] = binding[0].text
        equation_context.append(context)

    return equation_context


def get_query_string(variable_name, locality_uri):
    return ("select distinct ?variableName ?entityURI ?entityLabel ?equationString " 
            "from <" + locality_uri + "> "
            "where {"
                "?x <http://sadl.org/sadlimplicitmodel#descriptorName> ?variableName."
                "FILTER(REGEX(?variableName, '" + variable_name + "', 'i')) ."
                "OPTIONAL {?x <http://sadl.org/sadlimplicitmodel#augmentedType> ?augType ."
                "?augType <http://sadl.org/sadlimplicitmodel#semType> ?entityURI ."
                "?entityURI rdfs:label ?entityLabel  . }"
                "?list rdf:rest*/rdf:first ?x ."
                "?equation a <http://sadl.org/sadlimplicitmodel#ExternalEquation> ."
                "{?equation <http://sadl.org/sadlimplicitmodel#arguments> ?list .}" 
                "UNION"
                "{?equation <http://sadl.org/sadlimplicitmodel#returnTypes> ?list . }"
                "?equation <http://sadl.org/sadlimplicitmodel#expression> ?script ."
                "?script <http://sadl.org/sadlimplicitmodel#language> <http://sadl.org/sadlimplicitmodel#Text> ."
                "?script <http://sadl.org/sadlimplicitmodel#script> ?equationString ."
            "}")
