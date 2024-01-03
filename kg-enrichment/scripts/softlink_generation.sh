#!/bin/bash
declare -a lang_arr=("de" "fr" "es")
dir=`pwd`
echo $dir
if [[ ! -d enrichment-en ]]
then
 mkdir enrichment-en
 mkdir enrichment-en/en-files
 mkdir enrichment-en/fully-linked
 mkdir enrichment-en/linked-literals 
fi
files=`ls en/`
echo "Softlinking all files in english."
cd enrichment-en/en-files
for file in $files
do
#  echo "${file%%.*}"
 ln -s "${dir}/en/${file}" "${file%%.*}.nt"
done

for lang in "${lang_arr[@]}"
do
 echo "Softlinking necessary files from ${lang}"
 cd ../fully-linked
 ln -s "${dir}/enrichment-files/${lang}/fully_lnkd.nt" "${lang}_fl.nt"
 cd ../linked-literals
 ln -s "${dir}/enrichment-files/${lang}/lnkd_ltrls.nt" "${lang}_ll.nt"
done
echo "Softlinking files finished."
