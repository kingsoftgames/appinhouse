#!/bin/bash

APPINHOUSE_CONF_PATH=/srv/appinhouse/conf
REDIS_CONF_PATH=/srv/redis/conf

echo "init appinhouse conf path"
if [ ! -d "$APPINHOUSE_CONF_PATH" ]; then
    sudo mkdir -p "$APPINHOUSE_CONF_PATH"
    echo "mkdir "$APPINHOUSE_CONF_PATH
fi

echo "init redis conf path"
if [ ! -d "$REDIS_CONF_PATH" ]; then
    sudo mkdir -p "$REDIS_CONF_PATH"
    echo "mkdir "$REDIS_CONF_PATH
fi