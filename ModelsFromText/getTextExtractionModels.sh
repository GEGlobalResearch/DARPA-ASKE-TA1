#!/bin/bash
#
# Download Text Extraction Models from Github Releases.
#


DIR="$(pwd)"
echo $DIR

echo "=== Downloading models from https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/releases/download/TA-2019-10-22-Text-Extraction-Model/resources.zip ==="

wget https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/releases/download/TA-2019-10-22-Text-Extraction-Model/resources.zip

echo "=== Unzipping ... ==="

unzip resources.zip

echo "=== DONE ==="