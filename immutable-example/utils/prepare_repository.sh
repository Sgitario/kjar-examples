#!/bin/sh
REPOSITORY_PATH=$1
find $REPOSITORY_PATH -name *.lastUpdated -exec rm -v {} \;
find $REPOSITORY_PATH -name *.repositories -exec rm -v {} \;