#!/bin/bash

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/" && pwd )

if ls $DIR/lcm-packaging/target/lcm-complete-*-SNAPSHOT-bin.tar.gz 1> /dev/null 2>&1; then 
  package_name=`readlink -f $DIR/lcm-packaging/target/lcm-complete-*-SNAPSHOT-bin.tar.gz | xargs basename`
  version=${package_name#lcm-complete-*}
  version=${version%%-*}
  
  echo "Found Package: $package_name for version: v$version" 

  docker build -t kave/lcm:latest -t kave/lcm:v$version $DIR
else 
  echo "Couldn't find packaged lcm to build docker for"; 
fi 


