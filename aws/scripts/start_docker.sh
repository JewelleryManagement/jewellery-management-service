#!/bin/bash

# Set the environment variables for your application
JMS_DATABASE_NAME=$(aws ssm get-parameter --name JMS_DATABASE_NAME --query "Parameter.Value" --with-decryption --output text)
JMS_DATABASE_USER=$(aws ssm get-parameter --name JMS_DATABASE_USER --query "Parameter.Value" --with-decryption --output text)
JMS_DATABASE_PASSWORD=$(aws ssm get-parameter --name JMS_DATABASE_PASSWORD --query "Parameter.Value" --with-decryption --output text)

export JMS_DATABASE_NAME="$JMS_DATABASE_NAME"
export JMS_DATABASE_USER="$JMS_DATABASE_USER"
export JMS_DATABASE_PASSWORD="$JMS_DATABASE_PASSWORD"

service docker start
docker-compose build --no-cache --file /tmp/be/docker-compose.yml
docker-compose --file /tmp/be/docker-compose.yml up -d --build #> /logs/startup.log
