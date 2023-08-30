#!/bin/bash

docker-compose --file /tmp/be/docker-compose.yml down --remove-orphans #>> /logs/stop.log
