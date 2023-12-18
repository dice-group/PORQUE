#!/bin/bash
declare -a lang_arr=("de" "fr" "es")
if [[ ! -d enrichment-en ]]
then
 mkdir enrichment-en
 mkdir enrichment-en/en-files
 mkdir enrichment-en/fully-linked
 mkdir enrichment-en/linked-literals 
fi
files=`ls en/`
echo "Softlinking all files in english."
for file in $files
do
#  echo "${file%%.*}"
 ln -s "en/${file}" "enrichment-en/en-files/${file%%.*}.nt"
done

for lang in "${lang_arr[@]}"
do
 echo "Softlinking necessary files from ${lang}"
 ln -s "enrichment-files/${lang}/fully_lnkd" "enrichment-en/fully-linked/${lang}_fl.nt"
 ln -s "enrichment-files/${lang}/lnkd_ltrls" "enrichment-en/linked-literals/${lang}_ll.nt"
done
echo "Softlinking files finished."
