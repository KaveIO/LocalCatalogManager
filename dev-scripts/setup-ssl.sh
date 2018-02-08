#!/bin/bash

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/" && pwd )

mkdir -p $DIR/certificates
if [[  "$1" == "--force-overwrite" ]];
    then
	$DIR/create-certificates.sh -dir=$DIR/certificates  --force-overwrite
else
	$DIR/create-certificates.sh -dir=$DIR/certificates
fi