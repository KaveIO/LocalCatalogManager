#!/bin/bash
# This script populate data in a docker container with a mongo database. The server currently relies on 
# mongo and this can help fulfilling this requirement in demo scenarios. This is NOT MEANT 
# FOR PRODUCTION PURPOSES. This is for easy testing and demo purposes only. 

DOCKER_NAME="lcm-mongo"

docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"example2", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 58084631, "records": 3455461}, "data": { "uri": "csv://local/mock.csv"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"hive-default-sample-07", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "hive://remote-hive/sample_07"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"hive-foodmart-product", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "hive://remote-hive-foodmart/product"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"csvExample", "general": { "creation_date": "01-21-2016 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 58084631, "records": 3455461}, "data": { "uri": "csv://local/mock.csv","options" : { "table-description" : { "columns" : {"gender" : {}, "last_name": {}, "id": {}, "ip_address": {}, "first_name": {}, "email": {} }}}}})' localhost/lcm

# this is not needed for now docker exec -t $DOCKER_NAME mongo -eval 'db.taskschedule.insert({"items":[{"name":"Default Enrichment Task", "cron":"0 * * * * ?", "job":"nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask", "target":"*"}]})' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "local", "type": "csv", "options":[{"storagePath":"/tmp"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "remote-hive-sample", "type": "hive", "options":[{"username": "username", "password" : "password", "database": "sample", "url": "jdbc:hive2://10.191.0.1:10000", "driver" : "org.apache.hive.jdbc.HiveDriver"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "s3storage", "type": "s3", "options":[{"aws-access-key":"sample-acces-key", "aws-secret-access-key": "sample-secret-access-key", }]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "mongoStorage", "type": "mongo", "options":[{"username": "", "password" : "", "database": "lcm", "hostname": "localhost", "port" : "27017"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "jsonStorage", "type" : "json", "options" : { "storagePath" : "/tmp" }, "enrichment-properties" : { "cron-expression" : "  0 * * * * ?", "collected-properties" : [ "accessibility", "size", "structure", "last-time-scanned-at", "last-time-scanned-time" ] } })' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.remote_lcm.insert({"domain" : "0.0.0.0", "protocol" : "https", "port" :"4444"})' localhost/lcm
