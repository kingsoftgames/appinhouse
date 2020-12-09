#!/bin/bash

if [ ! -n "$1" ] ;then
    echo "please input appinhouse tag!"
    exit 1
fi

APPINHOUSE_HOME=/srv/appinhouse
cd $APPINHOUSE_HOME

export APPINHOUSE_IMAGE_TAG=$1

docker-compose down
docker-compose pull
docker-compose up -d
