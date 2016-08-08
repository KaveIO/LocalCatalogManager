#!/bin/bash

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" && pwd )

# Run the LCM server. It is started in a sub shell so the relative paths in the application.properties make sense.
(cd $DIR; java -jar -Dlog4j.configuration=file:$DIR/config/log4j.properties $DIR/lcm-runner-*.jar ui)
