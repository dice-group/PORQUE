#!/bin/bash
declare -a lang_arr=("de" "fr" "es" "en")
for lang in "${lang_arr[@]}"
do
 echo "Unzippin all files for ${lang}"
 filenames=`ls ${lang}/*.bz2`
  for eachfile in $filenames
  do
   echo $eachfile
   bzip2 -d "${eachfile}"
  done
done
echo "Unzipping files finished."
