#!/usr/bin/env bash
cd $PORQUE_WS/dockerfiles/
echo "Current directory: $(pwd)"
# remove the docker stack
echo "Shutting down docker containers"
docker-compose down
# echo "Stopping star-dog server"
# bash $STARDOG_HOME/stardog-admin server stop
echo "Waiting 20 seconds for processes to stop"
sleep 20s
echo "Services stopped"
