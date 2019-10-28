#!/bin/bash
#
# Start Models from Text Services.
#


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

