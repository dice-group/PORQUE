#!/bin/bash
if [[ ! -d extracted_mappings ]]
then
 mkdir extracted_mappings 
fi
declare -a lang_arr=("de" "fr" "es")
for lang in "${lang_arr[@]}"
do
 echo "Extracting links for ${lang}"
 grep -e "<http://dbpedia.org" "${lang}/interlanguage_links_${lang}.ttl" > "extracted_mappings/extracted_en-${lang}_links.ttl"
done
echo "Link mapping extraction finished."
