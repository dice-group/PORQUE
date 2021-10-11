#!/usr/bin/env bash

cd "${BASH_SOURCE%/*}" || exit

version=$(cat VERSION)
registry=${REGISTRY:-localhost:5000}

function build_container() {
    tag=$registry/porque/$1:$version
    echo "Building $1: $tag..."
    if docker build "${@:2}" -t "$tag"; then
        echo "Successfully built $tag."
        if docker push "$tag"; then
            echo "Successfully pushed $tag."
        fi
    fi
}
 cd ..
 git submodule update --init
 git submodule update --recursive --remote
 
 cd dockerfiles/
	 

 cd ../external-qa/qanary/qanary-core/qanary_pipeline-template/
 mvn clean install -Ddockerfile.skip=false -DskipTests

 cd ../../qanary-components/qanary_component-NED-DBpedia-Spotlight/
 mvn clean install -Ddockerfile.skip=false -DskipTests
 
 cd ../qanary_component-REL-RelationLinker2/
 mvn clean install -Ddockerfile.skip=false -DskipTests
 
 cd ../qanary_component-QB-Sina/
 mvn clean install -Ddockerfile.skip=false -DskipTests
 
 cd ../../../../dockerfiles/
 
build_container nginx .. -f nginx/Dockerfile
build_container backend-server .. -f backend/Dockerfile

export REGISTRY=$registry
export VERSION=$version

docker stack deploy --compose-file docker-compose.yml porque-stack
