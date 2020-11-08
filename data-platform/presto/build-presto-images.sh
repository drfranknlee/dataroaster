#!/bin/bash

set -e

REPONAME=mykidong
PRESTOVER=337

## Build Presto-server
docker build --build-arg PRESTO_VER=$PRESTOVER -t fb-presto presto/

# Tag and push to the public docker repository.
docker tag fb-presto $REPONAME/fb-presto:v337
docker push $REPONAME/fb-presto:v337

## Build Presto-CLI

TAG=presto-cli:v337

docker build --build-arg PRESTO_VER=$PRESTOVER -t $TAG presto-cli/

# Tag and push to the public docker repository.
docker tag $TAG $REPONAME/$TAG
docker push $REPONAME/$TAG