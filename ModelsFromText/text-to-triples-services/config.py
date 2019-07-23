import configparser


class Config:
    ElasticSearchServer = ''
    ElasticSearchIndex = ''
    NERModelFilePath = ''

    def __init__(self, config_file_path):
        config = configparser.ConfigParser()
        config.read(config_file_path)
        self.ElasticSearchServer = config["DEFAULT"]["ElasticSearchServer"]
        self.ElasticSearchIndex = config["DEFAULT"]["ElasticSearchIndex"]
        self.NERModelFilePath = config["DEFAULT"]["NERModelFilePath"]
