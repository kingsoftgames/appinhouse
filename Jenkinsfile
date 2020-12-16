#!/usr/bin/env groovy

pipeline {
    agent any
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
        booleanParam(name: 'AUTO_DEPLOY',
            defaultValue: true,
            description: 'When checked, will automatically deploy.')
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                }
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    dockerImage = dockerBuild project: 'infra', repo: 'appinhouse', push: true
                    echo "Built docker image: ${dockerImage}"
                }
            }
        }
        stage('Deploy') {
            when {
                expression {
                    return params.AUTO_DEPLOY
                }
            }
            steps {
                autoDeploy targetJob: 'Infra/Ops/Deploy/appinhouse'
            }
        }
    }
}
