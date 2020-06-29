#!/bin/sh
echo "Pre-loading Wikidata entries ..."

elasticsearch_loader --index wikidata_04022020 --type row csv /tmp/wikidata_entity_labels_04022020.csv

echo "DONE"
