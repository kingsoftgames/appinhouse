#!/bin/bash

APPINHOUSE_HOME=/srv/appinhouse
REDIS_CONFIG_PATH=/srv/appinhouse/redis/conf
APPINHOUSE_CONFIG_PATH=$APPINHOUSE_HOME/conf

cp /tmp/appinhouse/docker-compose.yml $APPINHOUSE_HOME
cp /tmp/appinhouse/redis.conf $REDIS_CONFIG_PATH
cp /tmp/appinhouse/app.conf $APPINHOUSE_CONFIG_PATH