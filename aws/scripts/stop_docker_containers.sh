docker compose -f ../docker/docker-compose.yml down > ~/logs/stop.log
docker compose -f ../docker/docker-compose.yml rm -f $(docker ps -a -q) >> ~/logs/stop.log
