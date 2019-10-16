#!/bin/sh
echo "Pre-loading Wikidata entries ..."

elasticsearch_loader --index wikidata --type row json /tmp/wikidata.json

echo "DONE"
