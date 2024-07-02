## Multilingual KG Enrichment for DBpedia 2016-10

Follow the steps below to obtain the set of enrichment files:

### Step 1: Download and Decompress Dataset

To download the dataset, run [scripts/dataset_download.sh](scripts/dataset_download.sh).

To decompress the downloaded data, run [scripts/unzip_files.sh](scripts/unzip_files.sh).

### Step 2: Extract Mappings

To extract the interlanguage mappings, run [scripts/extract-link-mappings.sh](scripts/extract-link-mappings.sh).

### Step 3: Setup SPARQL Endpoint

To run LIMES, you need to have a SPARQL endpoint, which you can set up using Virtuoso:

Once Virtuoso is installed, edit `virtuoso.ini.sample` to extend `AllowedDirs`, `MaxQueryTimeout`, and `Memory` requirements.

You can start the Virtuoso server with the following command:

```bash
./virtuoso-t +configfile ../database/virtuoso.ini.sample +f
```

To upload graphs to your Virtuoso endpoint, you need to connect it to its database through the shell:

```bash
./isql 1111 <username> <password>
```

Once the SQL shell is open, set up some write permissions:

```sql
grant execute on "DB.DBA.SPARQL_INS_OR_DEL_OR_MODIFY_CTOR_FIN" to "SPARQL";
grant execute on "DB.DBA.SPARQL_INSERT_CTOR_ACC" to "SPARQL";
grant execute on "DB.DBA.SPARQL_INSERT_DICT_CONTENT" to "SPARQL";
```

Now, start loading the graphs. To upload the ontology graph, run the following:

```sql
ld_dir('/data-disk/kg-fusion/ontology', '%.nt', 'http://www.upb.de/dbp2016-10-ontology');
rdf_loader_run();
checkpoint;
```

To insert the ontology into each language-specific graph, run the following for each language. Do not forget to replace `<insert-language-code-here>` with the language code, e.g., `en` / `de` / `fr` / `es`:

```sql
INSERT 
  { 
    GRAPH <http://www.upb.de/<insert-language-code-here>-dbp2016-10> { ?s ?p ?o } 
  }
WHERE
  { 
    GRAPH <http://www.upb.de/dbp2016-10-ontology>
    { 
      ?s ?p ?o
    } 
  }
```

To load language-specific data into the graphs, run the following after replacing `<path-to-your-dataset>`:

```sql
ld_dir('<path-to-your-dataset>/en', '%.ttl', 'http://www.upb.de/en-dbp2016-10');
ld_dir('<path-to-your-dataset>/de', '%.ttl', 'http://www.upb.de/de-dbp2016-10');
ld_dir('<path-to-your-dataset>/es', '%.ttl', 'http://www.upb.de/es-dbp2016-10');
ld_dir('<path-to-your-dataset>/fr', '%.ttl', 'http://www.upb.de/fr-dbp2016-10');
```

Check the load list with:

```sql
select * from DB.DBA.load_list;
```

Run the RDF loader and checkpoint with:

```sql
rdf_loader_run();
checkpoint;
```

### Step 4: Find missing links via LIMES

Firstly, follow the steps in [xml-config-generator.ipynb](xml-config-generator.ipynb) notebook to generate the configuration files.

Once the configuration files are ready, run LIMES for each configuration using the method described here: https://github.com/dice-group/LIMES?tab=readme-ov-file#running-limes.

After the N-Triples files are ready, merge them with their respective langauge specific versions in '/extracted_mappings' directory. You can use the 'cat' command, e.g.:

```bash
cat predictions_de.nt extracted_mappings/extracted_en-de_links.ttl > extracted_mappings/extracted_en-de_links.ttl
```

### Step 5: Additive data augmentation

Once all the extracted mappings are ready, proceed to follow steps in [additive-data-augmentation.ipynb](additive-data-augmentation.ipynb) notebook to generate enriched N-Triple files.

After the creation of enriched files, run [scripts/softlink_generation.sh](scripts/softlink_generation.sh) to organize all the enriched KG files into a single directory.

### Step 6: Setup Enriched KG Endpoint

You can use the same virtuoso instance to setup the enriched KG. First, make sure you are connected to SQL shell, then run the following after replacing `<path-to-your-dataset>`:

```sql
ld_dir('<path-to-your-dataset>/enrichment-en/linked-literals','%.nt','http://www.upb.de/en-dbp2016-10-enriched');
ld_dir('<path-to-your-dataset>/enrichment-en/fully-linked','%.nt','http://www.upb.de/en-dbp2016-10-enriched');
ld_dir('<path-to-your-dataset>/enrichment-en/en-files','%.nt','http://www.upb.de/en-dbp2016-10-enriched');
```

Check the load list with:

```sql
select * from DB.DBA.load_list;
```

Run the RDF loader and checkpoint with:

```sql
rdf_loader_run();
checkpoint;
```
