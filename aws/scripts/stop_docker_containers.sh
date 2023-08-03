#!/bin/bash

docker compose --file /tmp/docker-compose.yml down #> /logs/stop.log
docker compose --file /tmp/docker-compose.yml rm -f $(docker ps -a -q) #>> /logs/stop.log
