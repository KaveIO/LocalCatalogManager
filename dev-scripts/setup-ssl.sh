#!/bin/bash

# Find the base directory
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )/" && pwd )

mkdir -p $DIR/certificates
$DIR/create-certificates.sh $DIR/certificates $DIR
