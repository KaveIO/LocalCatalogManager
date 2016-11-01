#!/bin/bash
# This script populate data in a docker container with a mongo database. The server currently relies on 
# mongo and this can help fulfilling this requirement in demo scenarios. This is NOT MEANT 
# FOR PRODUCTION PURPOSES. This is for easy testing and demo purposes only. 

DOCKER_NAME="lcm-mongo"

docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"example2", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "csv://local/test.csv"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.taskschedule.insert({"items":[{"name":"Default Enrichment Task", "cron":"0 0 * * * ?", "job":"nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask", "target":"*"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "local", "options":[{"storagePath":"/tmp"}]})' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.fetch_endpoint.insert({"metadataID": "580a17dd29069ed99e86df82","creationDate": "01-01-2015 00:00:00","userToConsume": "admin","timeToLive": "01-01-2015 00:00:00"})' localhost/lcm