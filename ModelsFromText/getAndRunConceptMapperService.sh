#!/bin/bash
#
# Download And Run ConceptMapper service from Github Releases.
#

JAVA_HOME=/usr/java/latest

DIR="$(pwd)"
echo $DIR

echo "=== Downloading models from https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/releases/download/TA-2019-10-22-Text-Extraction-Model/concept-mapper-service.zip ==="

wget https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/releases/download/TA-2019-10-22-Text-Extraction-Model/concept-mapper-service.zip


echo "=== Unzipping ... ==="

unzip concept-mapper-service.zip

pkill -f concept-mapper-service/

cd concept-mapper-service/

$JAVA_HOME/bin/java -jar concept-mapper-service-0.0.1-SNAPSHOT.jar > ../logs/conceptMapperServices.log 2>&1 &

echo "=== DONE ==="