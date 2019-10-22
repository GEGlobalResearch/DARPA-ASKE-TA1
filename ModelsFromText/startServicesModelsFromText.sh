#!/bin/bash
#
# Start Models from Text Services.
#


# Before starting these services, ensure Docker container for elastic search is built and running
# To build and run Docker container for Elasticsearch run the following commands
# cd ModelsFromText/wikidata-for-elasticsearch
# docker build --tag=elasticsearch-aske 
# docker run -it -d elasticsearch-aske


DIR="$(pwd)"
echo $DIR

#source <set your env variables here>

source deactivate

source activate aske-ta1

conda info -e

echo "=== KILL TEXT TO PYTHON SERVICES... ==="
cd $DIR/text-to-python-service/
pkill -f "python app_text_to_python.py"

echo "=== START TEXT TO PYTHON MICROSERVICES... ==="
python app_text_to_python.py > $DIR/logs/text2Python.log 2>&1 &


echo "=== KILL TEXT TO TRIPLES SERVICES... ==="
cd $DIR/text-to-triples-services/
pkill -f "python app_text_to_triples.py"

echo "=== START TEXT TO TRIPLES MICROSERVICES... ==="
python app_text_to_triples.py app_resources/config.ini > $DIR/logs/text2Triples.log 2>&1 &


echo "=== DONE ==="

