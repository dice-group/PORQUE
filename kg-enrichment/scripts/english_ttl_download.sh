#!/bin/bash
set -eu
# Define the URL and file pattern
BASE_URL="https://downloads.dbpedia.org/2016-10/core-i18n/en/"
FILE_PATTERN="*.ttl.bz2"

# Create a directory to store downloaded files
DOWNLOAD_DIR="../dbpedia_downloads"
mkdir -p "$DOWNLOAD_DIR"
cd "$DOWNLOAD_DIR" || exit

# Excluding all the ttl files with NIF data, since it causes error
IGNORE_LIST=("nif_context_en.ttl.bz2" "nif_text_links_en.ttl.bz2" "nif_page_structure_en.ttl.bz2" "equations_en.ttl.bz2" "raw_tables_en.ttl.bz2")

# Create the ignore pattern for grep
IGNORE_PATTERN=$(printf "|%s" "${IGNORE_LIST[@]}")
IGNORE_PATTERN=${IGNORE_PATTERN:1} # Remove the leading '|'

# Get the list of .ttl.bz2 files from the URL
echo "Fetching list of files..."
wget -q -O- "$BASE_URL" | grep -oP 'href="\K[^"]*\.ttl\.bz2' > files.txt

# Filter out the files to ignore
if [ -n "$IGNORE_PATTERN" ]; then
  grep -v -E "$IGNORE_PATTERN" files.txt > filtered_files.txt
else
  cp files.txt filtered_files.txt
fi

# Download the files using wget with parallel
echo "Starting download of .ttl.bz2 files..."
cat filtered_files.txt | parallel -j 0 wget -q --show-progress "$BASE_URL{}"

# Extract the downloaded .ttl.bz2 files using lbzip2 with parallel
echo "Extracting .ttl files from .ttl.bz2 archives..."
ls *.ttl.bz2 | parallel -j 0 lbzip2 -dk --verbose

# Remove the .ttl.bz2 files after extraction
echo "Cleaning up .ttl.bz2 files..."
rm *.ttl.bz2

echo "Download and extraction completed."
