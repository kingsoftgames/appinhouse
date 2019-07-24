#!/usr/bin/env groovy
appinhousePath = '/opt/nobody/'
appinhouseServerPath = appinhousePath + 'appinhouseserver/'
appinhouseWebPath = appinhousePath + 'appinhouseweb/'

def loadAppinhouseConf() {
    def resource = libraryResource 'appinhouse.conf'
    return readProperties(text: resource)
}

def getHost() {
    def appinhouseConf = loadAppinhouseConf()
    def remote = [:]
    remote.name = appinhouseConf.name
    remote.host = appinhouseConf.host
    remote.allowAnyHosts = true
    remote.user = appinhouseConf.userName
    return remote
}

remote = getHost()

// First run 
def getInitSh() {
    def initSh = '''\
#!/bin/bash
SERVER_FILE_DIR=appinhouseServerPath
WEB_FILE_DIR=appinhouseWebPath

echo "init appinhouse path"
if [ ! -d "$SERVER_FILE_DIR" ]; then  
    sudo mkdir -p "$SERVER_FILE_DIR"
    echo "mkdir "$SERVER_FILE_DIR 
    sudo chown -R  nobody:nogroup $SERVER_FILE_DIR
fi 

if [ ! -d "$WEB_FILE_DIR" ]; then  
    sudo mkdir -p "$WEB_FILE_DIR" 
    echo "mkdir "$WEB_FILE_DIR
    sudo chown -R  nobody:nogroup $WEB_FILE_DIR 
fi 
'''
    return initSh.replaceAll("appinhouseServerPath", appinhouseServerPath)
            .replaceAll("appinhouseWebPath", appinhouseWebPath)
    
}

// run after scp server artifact and appinhouse.service
def getServerSh(String secretKey) {
    def serverSh = '''\
#!/bin/bash

SERVER_FILE_PATH=serverFilePath
SERVER_FILE_DIR=${SERVER_FILE_PATH%.tar.gz*}
CURRENT_FILE_DIR=currentFileDir
APPINHOUSE_SERVICE_DIR=appinhouseServiceDir
APPINHOUSE_SERVER_TMP_PATH=appinhouseServerTmpPath
APPINHOUSE_SERVICE_TMP_PATH=appinhouseServiceTmpPath

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
sudo sed -i '/^secret_key/csecret_key = secretKeyValue' $SERVER_FILE_DIR/conf/app.conf

sudo ln -snf $SERVER_FILE_DIR  $CURRENT_FILE_DIR

sudo chown -R nobody:nogroup $SERVER_FILE_DIR
sudo chown -h nobody:nogroup $CURRENT_FILE_DIR

echo "system appinhouse.service"

if [ ! -f "$APPINHOUSE_SERVICE_DIR" ]; then
    sudo mkdir -p $APPINHOUSE_SERVICE_DIR
fi
sudo cp $APPINHOUSE_SERVICE_TMP_PATH $APPINHOUSE_SERVICE_DIR
sudo systemctl enable appinhouse.service
sudo systemctl restart appinhouse.service
'''
    return serverSh.replaceAll("serverFilePath", appinhouseServerPath + env.SERVER_TARBALL)
            .replaceAll("currentFileDir", appinhouseServerPath + "current")
            .replaceAll("appinhouseServiceDir", "/usr/lib/systemd/system/")
            .replaceAll("appinhouseServerTmpPath", "/tmp/" + env.SERVER_TARBALL)
            .replaceAll("appinhouseServiceTmpPath", "/tmp/appinhouse.service")
            .replaceAll("secretKeyValue", secretKey)
    
}

// run after scp web artifact
def getWebSh() {
    def webSh = '''\
#!/bin/bash
WEB_FILE_PATH=webFilePath
WEB_FILE_DIR=${WEB_FILE_PATH%.tar.gz*}
CURRENT_FILE_DIR=currentFileDir
APPINHOUSE_WEB_TMP_PATH=appinhouseWebTmpPath
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
'''
    return webSh.replaceAll("webFilePath", appinhouseWebPath + env.WEB_TARBALL)
            .replaceAll("currentFileDir", appinhouseWebPath + "current")
            .replaceAll("appinhouseWebTmpPath", "/tmp/" +  env.WEB_TARBALL)
    
}

// run after scp appinhouse.conf for nginx
def getNginxSh() {
    def nginxSh = '''\
#!/bin/bash

CONF_TMP_PATH=confTmpPath

if [ ! -f "$CONF_TMP_PATH" ]; then  
    echo $CONF_TMP_PATH" not exist "
    exit 1
fi
sudo cp $CONF_TMP_PATH /etc/nginx/conf.d/
sudo systemctl restart nginx.service
'''
    return nginxSh.replaceAll("confTmpPath", "/tmp/appinhouse.conf")
}

def deployAppinhouseServer(String secretKey) {
    sshPut remote: remote, from: env.SERVER_TARBALL, into: '/tmp'
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/appinhouse.service', into: '/tmp'
    writeFile file: 'server.sh', text: getServerSh(secretKey)
    sshScript remote: remote, script: 'server.sh'
    sshRemove remote: remote, path: '/tmp/' + env.SERVER_TARBALL
}

def deployAppinhouseWeb() {
    sshPut remote: remote, from: env.WEB_TARBALL, into: '/tmp'
    writeFile file: 'web.sh', text: getWebSh()
    sshScript remote: remote, script: 'web.sh'
    sshRemove remote: remote, path: '/tmp/' + env.WEB_TARBALL
}

def deployAppinhouseNginxConf() {
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/appinhouse.conf', into: '/tmp'
    writeFile file: 'nginx.sh', text: getNginxSh()
    sshScript remote: remote, script: 'nginx.sh'
}

pipeline {
    agent {
        label "os:linux"
    }
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(
            daysToKeepStr: '15',
            artifactNumToKeepStr: '20'
        ))
        ansiColor('xterm')
    }
    parameters {
        booleanParam(name: 'DEPLOY_SERVER',
            defaultValue: false,
            description: 'When checked, will automatically deploy server (backend).')
        booleanParam(name: 'DEPLOY_WEB',
            defaultValue: false,
            description: 'When checked, will automatically deploy web (frontend).')
        booleanParam(name: 'DEPLOY_NGINX',
            defaultValue: false,
            description: 'When checked, will automatically deploy nginx (frontend).')
    }
    environment {
        GITHUB_URL = 'https://github.com/rog2/appinhouse'
        GO_VERSION = '1.10'
        GOPATH = "${env.WORKSPACE}"
        SERVER_ROOT = "${env.WORKSPACE}/src/appinhouse/server"
        WEB_ROOT = "${env.WORKSPACE}/src/appinhouse/web"
        SERVER_TARBALL = artifactName(name: 'appinhouse', extension: 'server.tar.gz')
        WEB_TARBALL = artifactName(name: 'appinhouse', extension: 'web.tar.gz')
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                    userRemoteConfigs: [[url: env.GITHUB_URL]],
                    branches: [[name: env.BRANCH_NAME ?: 'master']],
                    browser: [$class: 'GithubWeb', repoUrl: env.GITHUB_URL],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src/appinhouse']]
                ])
            }
        }
        stage('Remove Last Build') {
            steps {
                sh 'rm -vf *.tar.gz'
                sh 'rm -vf *.sh'
            }
        }
        stage('Build Server') {
            steps {
                dir (env.SERVER_ROOT) {
                    withGo(env.GO_VERSION) {
                        sh """
                            go get -v github.com/beego/bee
                            go get -v
                            go build -o appinhouse
                            ${env.GOPATH}/bin/bee pack -o ${env.WORKSPACE} -exr pack.sh -exr server -exr test.conf -exr bin
                        """
                    }
                }
                sh "mv server.tar.gz ${env.SERVER_TARBALL}"
            }
        }
        stage('Build Web') {
            steps {
                dir (env.WEB_ROOT) {
                    sh "tar czvf ${env.WORKSPACE}/${env.WEB_TARBALL} static"
                }
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: '*.tar.gz', onlyIfSuccessful: true
            }
        }
        stage("Init work dir!") {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins.ssh', keyFileVariable: 'identity')]) {
                        remote.identityFile = identity
                        writeFile file: 'init-dir.sh', text: getInitSh()
                        sshScript remote: remote, script: 'init-dir.sh'
                    }   
                }
            }
        }
        stage('Deploy Server') {
            when {
                expression {
                    return params.DEPLOY_SERVER
                }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins.ssh', keyFileVariable: 'identity')]) {
                        remote.identityFile = identity
                        def defaults = loadEnv.getDefaults()
                        withCredentials([string(credentialsId: defaults['appinhouse.credential'], variable: 'secretKey')]) {
                            deployAppinhouseServer(secretKey)
                        }
                    }
                }
            }
        }
        stage('Deploy Web') {
            when {
                expression {
                    return params.DEPLOY_WEB
                }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins.ssh', keyFileVariable: 'identity')]) {
                        remote.identityFile = identity
                        deployAppinhouseWeb()
                    }   
                }
            }
        }
        stage('Deploy Nginx conf') {
            when {
                expression {
                    return params.DEPLOY_NGINX
                }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins.ssh', keyFileVariable: 'identity')]) {
                        remote.identityFile = identity
                        deployAppinhouseNginxConf()
                    }
                }
            }
        }
    }
}
