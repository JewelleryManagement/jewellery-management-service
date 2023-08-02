#!/bin/bash

docker compose -f /tmp/docker-compose.yml down #> /logs/stop.log
docker compose -f /tmp/docker-compose.yml rm -f $(docker ps -a -q) #>> /logs/stop.log
