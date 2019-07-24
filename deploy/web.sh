#!/bin/bash

if [ ! -n "$1" ] ;then
    echo "please input appinhouse web artifact!"
    exit 1
fi
WEB_HOME_PATH=/opt/nobody/appinhouseweb
WEB_FILE_NAME=$1
WEB_FILE_PATH=$WEB_HOME_PATH/$WEB_FILE_NAME
WEB_FILE_DIR=${WEB_FILE_PATH%.tar.gz*}
CURRENT_FILE_DIR=$WEB_HOME_PATH/current
APPINHOUSE_WEB_TMP_PATH=/tmp/$WEB_FILE_NAME

echo "init appinhouse web path"
if [ ! -d "$WEB_HOME_PATH" ]; then  
    sudo mkdir -p "$WEB_HOME_PATH"
    echo "mkdir "$WEB_HOME_PATH 
    sudo chown -R  nobody:nogroup $WEB_HOME_PATH
fi 

echo 'unzip web'

if [ ! -f "$APPINHOUSE_WEB_TMP_PATH" ]; then  
    echo $APPINHOUSE_WEB_TMP_PATH" not exist "
    exit 1
fi

if [ ! -d "$WEB_FILE_DIR" ]; then
    sudo mkdir -p $WEB_FILE_DIR
    sudo tar -zxvf $APPINHOUSE_WEB_TMP_PATH -C $WEB_FILE_DIR
fi

sudo chown -R  nobody:nogroup $WEB_FILE_DIR

sudo ln -snf $WEB_FILE_DIR  $CURRENT_FILE_DIR

sudo chown -h nobody:nogroup $CURRENT_FILE_DIR