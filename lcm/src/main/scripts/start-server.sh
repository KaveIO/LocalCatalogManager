#!/bin/bash

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" && pwd )

# Run the LCM server
java -jar -Dlog4j.configuration=file:$DIR/config/log4j.properties LocalCatalogManager-0.1-SNAPSHOT.jar server