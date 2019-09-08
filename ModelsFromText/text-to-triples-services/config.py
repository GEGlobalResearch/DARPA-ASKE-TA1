import configparser


class Config:
    ElasticSearchServer = ''
    ElasticSearchIndex = ''
    NERModelFilePath = ''
    TripleStoreURL = ''
    NLPServiceURL = ''
    UnitsOntologyPath = ''
    # synonyms will be provided as 'pipe' | separated vaules
    UnitsSynonymURIList = ''
    UnitsOntologyGraphURI = ''

    def __init__(self, config_file_path):
        config = configparser.ConfigParser()
        config.read(config_file_path)
        self.ElasticSearchServer = config["DEFAULT"]["ElasticSearchServer"]
        self.ElasticSearchIndex = config["DEFAULT"]["ElasticSearchIndex"]
        self.NERModelFilePath = config["DEFAULT"]["NERModelFilePath"]
        self.TripleStoreURL = config["DEFAULT"]["TripleStoreURL"]
        self.NLPServiceURL = config["DEFAULT"]["NLPServiceURL"]
        self.UnitsOntologyPath = config["DEFAULT"]["UnitsOntologyPath"]
        self.UnitsSynonymURIList = config["DEFAULT"]["UnitsSynonymURIList"]
        self.UnitsOntologyGraphURI = config["DEFAULT"]["UnitsOntologyGraphURI"]
