#!/usr/bin/env sh
VERSION=$(cat gradle.properties | grep version | awk -F '=' '{ print $2 }')
./gradlew clean server:dockerBuildNative && \
  docker rm -f auth && \
  docker run -d \
  --name auth \
  --restart=unless-stopped \
  -p 8066:80 \
  -e DB_HOST=host.docker.internal \
  -e MANAGEMENT_PORT=80 \
  -e AUTH_TOKEN_DOMAIN=local.mooglest.com \
  -e RABBITMQ_URI=amqp://host.docker.internal \
  -e RABBITMQ_EXCHANGE=mooglest \
  "auth:$VERSION"
