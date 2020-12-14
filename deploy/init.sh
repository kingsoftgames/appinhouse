#!/bin/bash

APPINHOUSE_CONF_PATH=/srv/appinhouse/conf
REDIS_CONF_PATH=/srv/appinhouse/redis/conf
REDIS_DATA_PATH=/srv/appinhouse/redis/data

echo "init appinhouse conf path"
if [ ! -d "$APPINHOUSE_CONF_PATH" ]; then
    mkdir -p "$APPINHOUSE_CONF_PATH"
    echo "mkdir "$APPINHOUSE_CONF_PATH
    chmod 766 $APPINHOUSE_CONF_PATH
fi

echo "init redis conf path"
if [ ! -d "$REDIS_CONF_PATH" ]; then
    mkdir -p "$REDIS_CONF_PATH"
    echo "mkdir "$REDIS_CONF_PATH
    chmod 766 $REDIS_CONF_PATH
fi

echo "init redis data path"
if [ ! -d "$REDIS_DATA_PATH" ]; then
    mkdir -p "$REDIS_DATA_PATH"
    echo "mkdir "$REDIS_DATA_PATH
    chmod 766 $REDIS_DATA_PATH
fi