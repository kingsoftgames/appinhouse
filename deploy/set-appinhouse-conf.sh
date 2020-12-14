#!/bin/bash

APPINHOUSE_CONF_PATH=/srv/appinhouse/conf

if [ ! -n "$1" ] ;then
    echo "please input secret key!"
    exit 1
fi
SECRET_KEY=$1
echo "init redis conf path"
sed -i '/^secret_key/csecret_key = '$SECRET_KEY $APPINHOUSE_CONF_PATH/app.conf
sed -i '/^addr/caddr = 'redis:6379 $APPINHOUSE_CONF_PATH/app.conf
