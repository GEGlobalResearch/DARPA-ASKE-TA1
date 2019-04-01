from flair.data import Sentence
from flair.models import SequenceTagger


def load_model(model_file_path):
    model = SequenceTagger.load_from_file(model_file_path)
    return model


# operates at the sentence level
def extract(model, text):
    sentence = Sentence(text)
    model.predict(sentence)
    entity_dict = sentence.to_dict(tag_type='ner')
    return entity_dict
