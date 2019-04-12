from flair.data import Sentence
from flair.models import SequenceTagger
from segtok.segmenter import split_single


def eq_context_from_concept(entity, sent, line):
    start_idx = entity["end_pos"] + 1
    symbol_str = ""
    for i in range(start_idx, len(sent)):
        if sent[i] == " ":
            break
        else:
            symbol_str = symbol_str + sent[i]
    symbol_dict = {}
    if symbol_str != "":
        symbol_dict = {"text": entity["text"], "symbol": symbol_str, "line": line}
    return symbol_dict


def get_data_descriptor_uri(symbol, triples):
    data_desc_uri = "NA"
    for triple in triples:
        obj_str = (triple["object"]).replace("\"", "")
        if ("data_" in triple["subject"] or "return_" in triple["subject"]) and (symbol == obj_str):
            data_desc_uri = triple["subject"]
            break
    return data_desc_uri

