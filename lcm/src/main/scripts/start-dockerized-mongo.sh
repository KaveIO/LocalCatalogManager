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

sleep 5 
mongo -eval 'db.metadata.insert({"name":"example", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "file://local/user/bob/train_schedule"}})' localhost/lcm
mongo -eval 'db.taskschedule.insert({"items":[{"name":"Default Enrichment Task", "cron":"0 0 * * * ?", "job":"nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask", "target":"*"}]})' localhost/lcm
