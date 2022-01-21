#!/bin/bash
declare -a lang_arr=("en" "de" "fr" "es")
declare -a dataset_arr=("article_categories" "category_labels" "commons_page_links" "disambiguations" "instance_types" "interlanguage_links_chapters" "labels" "long_abstracts" "mappingbased_literals" "mappingbased_objects" "persondata" "short_abstracts" "skos_categories")
download_prefix="http://downloads.dbpedia.org/2016-10/core-i18n/"
download_postfix=".ttl.bz2"

for lang in "${lang_arr[@]}" 
do
  mkdir $lang
  for dataset in "${dataset_arr[@]}" 
  do
    download_url="${download_prefix}${lang}/${dataset}_${lang}${download_postfix}"
    echo "Downloading ${download_url}"
    wget -P $lang $download_url
  done
done
wget -P en/ http://downloads.dbpedia.org/2016-10/core-i18n/en/anchor_text_en.ttl.bz2
wget -P en/ http://downloads.dbpedia.org/2016-10/core-i18n/en/genders_en.ttl.bz2
wget -P fr/ http://downloads.dbpedia.org/2016-10/core-i18n/fr/french_population_fr.ttl.bz2
echo "Downloads finished."
