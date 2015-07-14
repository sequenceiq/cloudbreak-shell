#!/bin/bash

: ${JAR_PATH:=/tmp/cloudbreak-shell.jar}
SNAPSHOT_URL=http://maven.sequenceiq.com/releases
PACKAGE=com/sequenceiq
ARTIFACT=cloudbreak-shell
FULLNAME=$PACKAGE/$ARTIFACT

VERSION=$(curl -Ls $SNAPSHOT_URL/$FULLNAME/maven-metadata.xml|sed -n "s/.*<version>\([^<]*\).*/\1/p" |tail -1)

LATEST=$(curl -Ls $SNAPSHOT_URL/$FULLNAME/$VERSION/maven-metadata.xml|sed -n "/>jar</ {n;s/.*<value>\([^<]*\).*/\1/p;}"|tail -1)

echo latest jar version is $VERSION ...
echo downloading exetuable $SNAPSHOT_URL/$PACKAGE/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION.jar jar into $JAR_PATH ...
curl -o $JAR_PATH $SNAPSHOT_URL/$PACKAGE/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION.jar

echo To start cloudbreak-shell type:
echo =========================================
echo java -jar $JAR_PATH
echo =========================================