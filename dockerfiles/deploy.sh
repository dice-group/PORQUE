#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}" || exit

cd ..
git submodule update --init
git submodule update --recursive --remote

# Starting Stardog server
# bash $STARDOG_HOME/stardog-admin server start

echo "Checking availability of Stardog server."
# Check if Stardog server is up
STATUSCODE=$(curl --silent --output /dev/stderr --write-out "%{http_code}" http://admin:admin@localhost:5820/qanary)
if [ "$STATUSCODE" -ne 200 ]; then echo "Stardog server unreachable."; exit -1; fi

echo "Checking availability of ES instance."
# Check if ES server is up
STATUSCODE=$(curl --silent --output /dev/stderr --write-out "%{http_code}" http://porque.cs.upb.de:9400)
if [ "$STATUSCODE" -ne 200 ]; then echo "ElasticSearch server unreachable."; exit -1; fi

cd dockerfiles/


cd ../external-qa/qanary/qanary-core/qanary_pipeline-template/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../../qanary-components/qanary_component-NED-DBpedia-Spotlight/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-REL-RelationLinker2/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-QB-Sina/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-NED-Falcon/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-NED-Falcon-Enriched/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-REL-RelationLinker3/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../qanary_component-QB-SQG/
mvn clean install -Ddockerfile.skip=false -DskipTests

cd ../../../../dockerfiles/
 
cd ..
docker build -t porque-nginx -f dockerfiles/nginx/Dockerfile .
docker build -t porque-backend -f dockerfiles/backend/Dockerfile .

cd dockerfiles/
# docker stack deploy --compose-file ./dockerfiles/docker-compose.yml porque-stack
docker-compose up -d
