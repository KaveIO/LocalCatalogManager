#!/bin/bash
# This script starts a docker container with a mongo database. The server currently relies on
# mongo and this can help fulfilling this requirement in demo scenarios. This is NOT MEANT
# FOR PRODUCTION PURPOSES. This is for easy testing and demo purposes only.

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" && pwd )
DOCKER_NAME="lcm-mongo"

RUNNING=`docker ps -q -f name=$DOCKER_NAME`
STOPPED=`docker ps -q -a -f name=$DOCKER_NAME`

if [ "$RUNNING" != "" ]; then
    echo "MongoDB already running. Ignoring command."
    exit
fi

if [ "$STOPPED" != "" ]; then
    echo "MongoDB stopped. Restarting."
    docker start $DOCKER_NAME
fi

echo "No MongoDB detected. Creating container."
docker run --name $DOCKER_NAME -p 127.0.0.1:27017:27017 -d mongo:3
