#!/bin/bash

: ${JAR_PATH:=/tmp/cloudbreak-shell.jar}

curl -o $JAR_PATH https://s3-eu-west-1.amazonaws.com/maven.sequenceiq.com/snapshots/com/sequenceiq/cloudbreak/cloudbreak-shell/0.1-SNAPSHOT/cloudbreak-shell-0.1-20140814.082233-62.jar


echo To start cloudbreak-shell type:
echo =========================================
echo java -jar $JAR_PATH
echo =========================================