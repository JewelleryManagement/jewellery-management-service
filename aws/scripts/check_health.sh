#!/bin/bash

# Wait for the application to start (you may need to adjust this based on your app startup time)
sleep 20

# Perform the health check
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health | grep -q "200"; then
    echo "Application is healthy."
    exit 0  # Success
else
    echo "Application health check failed."
    exit 1  # Failure
fi
