#!/bin/bash

if [ ! -n "$1" ] ;then
    echo "please input appinhouse server artifact!"
    exit 1
fi
if [ ! -n "$2" ] ;then
    echo "please input secret key!"
    exit 1
fi

SERVER_HOME_PATH=/opt/nobody/appinhouseserver
SERVER_FILE_NAME=$1
SERVER_FILE_PATH=$SERVER_HOME_PATH/$SERVER_FILE_NAME
SERVER_FILE_DIR=${SERVER_FILE_PATH%.tar.gz*}
CURRENT_FILE_DIR=$SERVER_HOME_PATH/current
APPINHOUSE_SERVICE_DIR=/usr/lib/systemd/system/
APPINHOUSE_SERVER_TMP_PATH=/tmp/$SERVER_FILE_NAME
APPINHOUSE_SERVICE_TMP_PATH=/tmp/appinhouse.service
SECRET_KEY=$2
echo "init appinhouse server path"
if [ ! -d "$SERVER_HOME_PATH" ]; then  
    sudo mkdir -p "$SERVER_HOME_PATH"
    echo "mkdir "$SERVER_HOME_PATH 
    sudo chown -R  nobody:nogroup $SERVER_HOME_PATH
fi 


if [ ! -f "$APPINHOUSE_SERVER_TMP_PATH" ]; then  
    echo $APPINHOUSE_SERVER_TMP_PATH" not exist "
    exit 1
fi
if [ ! -f "$APPINHOUSE_SERVICE_TMP_PATH" ]; then
    echo $APPINHOUSE_SERVICE_TMP_PATH" not exist "
    exit 1
fi

echo 'unzip server'
if [ ! -d "$SERVER_FILE_DIR" ]; then
    sudo mkdir -p $SERVER_FILE_DIR
    sudo tar -zxvf $APPINHOUSE_SERVER_TMP_PATH -C $SERVER_FILE_DIR
fi
sudo sed -i '/^secret_key/csecret_key = '$SECRET_KEY $SERVER_FILE_DIR/conf/app.conf

sudo ln -snf $SERVER_FILE_DIR  $CURRENT_FILE_DIR

sudo chown -R nobody:nogroup $SERVER_FILE_DIR
sudo chown -h nobody:nogroup $CURRENT_FILE_DIR

echo "system appinhouse.service"

if [ ! -d "$APPINHOUSE_SERVICE_DIR" ]; then
    sudo mkdir -p $APPINHOUSE_SERVICE_DIR
fi
sudo cp $APPINHOUSE_SERVICE_TMP_PATH $APPINHOUSE_SERVICE_DIR
sudo systemctl enable appinhouse.service
sudo systemctl restart appinhouse.service