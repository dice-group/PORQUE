#!/usr/bin/env bash

# remove the docker stack
echo "Removing PORQUE's docker stack"
docker stack rm porque-stack
echo "Stopping star-dog server"
bash $STARDOG_HOME/stardog-admin server stop
echo "Waiting 20 seconds for processes to stop"
sleep 20s
echo "Services stopped"
