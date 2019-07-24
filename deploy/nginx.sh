#!/bin/bash

CONF_TMP_PATH=/tmp/appinhouse.conf

if [ ! -f "$CONF_TMP_PATH" ]; then  
    echo $CONF_TMP_PATH" not exist "
    exit 1
fi
sudo cp $CONF_TMP_PATH /etc/nginx/conf.d/
sudo systemctl restart nginx.service