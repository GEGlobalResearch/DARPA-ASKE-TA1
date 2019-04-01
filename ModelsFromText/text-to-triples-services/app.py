import connexion
import text_to_triples_service as t2t
import locality_search as locality
from flask_cors import CORS
import extract_concepts_equations as extract
import entity_linking as el
import triple_generation as tp
import equation_context as context
from segtok.segmenter import split_single


def text_to_triples(body):
    return t2t.text_to_triples(body)


def get_equation_var_context(body):
    return locality.get_equation_var_context(body)


def process_example_doc(body):

    file_path = "demo/" + str(body["docId"]) + ".txt"
    file = open(file_path, 'r')

    content = file.readlines()
    text = ""

    for con in content:
        text = text + " " + con

    return t2t.text_to_triples({"text": text, "locality": "string"})


if __name__ == '__main__':
    app = connexion.App(__name__, specification_dir='swagger/')
    app.add_api('text2triplesapi.yaml')
    CORS(app.app)
    application = app.app
    app.run(port=4200)
