#!/bin/bash

LOCALDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

./gradlew assembleDist
docker-compose build
docker-compose up -d

rm -rf $LOCALDIR/test-results
mkdir $LOCALDIR/test-results
sleep 10
docker run --link muonjava_rabbitmq_1:rabbitmq  -v $LOCALDIR/test-results:/app/test-results simplicityitself/muon-amqp-protocol-spec

docker-compose stop --timeout 2

echo "Tests Completed, results are at $LOCALDIR/test-results"
