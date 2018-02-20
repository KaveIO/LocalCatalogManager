#!/bin/bash
# This script populate data in a docker container with a mongo database. The server currently relies on
# mongo and this can help fulfilling this requirement in demo scenarios. This is NOT MEANT
# FOR PRODUCTION PURPOSES. This is for easy testing and demo purposes only.

# for more templates open wiki page:
# https://github.com/DataAnalyticsOrganization/LocalCatalogManager/wiki/Mongo-object-templates

DOCKER_NAME="lcm-mongo"

docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"example2", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 58084631, "records": 3455461}, "data": { "uri": "csv://local/mock.csv"}})' localhost/lcm

# this is not needed for now docker exec -t $DOCKER_NAME mongo -eval 'db.taskschedule.insert({"items":[{"name":"Default Enrichment Task", "cron":"0 * * * * ?", "job":"nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask", "target":"*"}]})' localhost/lcm

#The
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "local", "type": "csv", "options":{"storagePath":"/tmp"}})' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.remote_lcm.insert({"domain" : "0.0.0.0", "protocol" : "https", "port" :"4444"})' localhost/lcm
