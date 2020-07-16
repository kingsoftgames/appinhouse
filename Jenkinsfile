#!/usr/bin/env groovy
def loadAppinhouseConf() {
    def resource = libraryResource 'appinhouse.yml'
    return readYaml(text: resource)
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

def deployAppinhouseServer(String secretKey, String domain) {
    sshPut remote: remote, from: env.SERVER_TARBALL, into: '/tmp'
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/appinhouse.service', into: '/tmp'
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/server.sh', into: '/tmp'
    sshCommand remote: remote, command: "bash /tmp/server.sh "+ env.SERVER_TARBALL + " " + secretKey + " " + domain 
    sshRemove remote: remote, path: '/tmp/' + env.SERVER_TARBALL
}

def deployAppinhouseWeb() {
    sshPut remote: remote, from: env.WEB_TARBALL, into: '/tmp'
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/web.sh', into: '/tmp'
    sshCommand remote: remote, command: "bash /tmp/web.sh "+ env.WEB_TARBALL
    sshRemove remote: remote, path: '/tmp/' + env.WEB_TARBALL
}

def deployAppinhouseNginxConf() {
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/appinhouse.conf', into: '/tmp'
    sshPut remote: remote, from: env.WORKSPACE + '/src/appinhouse/deploy/nginx.sh', into: '/tmp'
    sshCommand remote: remote, command: "bash /tmp/nginx.sh"
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
                        def appinhouseConf = loadAppinhouseConf()
                        withCredentials([string(credentialsId: appinhouseConf.credential, variable: 'secretKey')]) {
                            deployAppinhouseServer(secretKey, appinhouseConf.domain)
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
