#!/bin/bash
# This script populate data in a docker container with a mongo database. The server currently relies on 
# mongo and this can help fulfilling this requirement in demo scenarios. This is NOT MEANT 
# FOR PRODUCTION PURPOSES. This is for easy testing and demo purposes only. 

DOCKER_NAME="lcm-mongo"

docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"example2", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "csv://local/test.csv"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"hive-default-sample-07", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "hive://remote-hive/sample_07"}})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.metadata.insert({"name":"hive-foodmart-product", "general": { "creation_date": "01-01-2015 00:00:00", "owner": "bob", "description": "This is the newly computed train schedule for optimized transport flow.", "tags": ["train", "svm", "lolwut"], "size": 351684984631, "records": 3455461}, "data": { "uri": "hive://remote-hive-foodmart/product"}})' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.taskschedule.insert({"items":[{"name":"Default Enrichment Task", "cron":"0 0 * * * ?", "job":"nl.kpmg.lcm.server.task.enrichment.DataEnrichmentTask", "target":"*"}]})' localhost/lcm

docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "local", "options":[{"storagePath":"/tmp"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "remote-hive", "options":[{"username": "hive", "password" : "hive", "database": "default", "url": "jdbc:hive2://10.191.30.201:10000", "driver" : "org.apache.hive.jdbc.HiveDriver"}]})' localhost/lcm
docker exec -t $DOCKER_NAME mongo -eval 'db.storage.insert({"name": "remote-hive-foodmart", "options":[{"username": "hive", "password" : "hive", "database": "foodmart", "url": "jdbc:hive2://10.191.30.201:10000", "driver" : "org.apache.hive.jdbc.HiveDriver"}]})' localhost/lcm
