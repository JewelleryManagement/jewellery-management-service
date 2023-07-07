FROM postgres:latest

ENV POSTGRES_DB=jewellery-management
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=V*BjZ(@AAh%Ec@5j

ADD ./docker-entrypoint-initdb.d/init.sql /docker-entrypoint-initdb.d/