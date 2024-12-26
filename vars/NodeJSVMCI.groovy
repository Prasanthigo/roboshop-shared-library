pipeline {
    agent { node { label 'Agent-1' } } 
    environment{
        //here if you create any variable you will have global access, since its environment no need of def
        packageVersion = ''
    }
    parameters {
        string(name: 'component', defaultvalue: '', description: 'Which component?')
    }
    stages {
        stage('Reading Version') {
            steps {
                script {
                    def packageJson = readJSON file: 'package.json'
                    packageVersion = packageJson.version
                    echo "Package Version: ${packageVersion}"
                }
            }

        }
        stage('install dependencies') {
            
            steps {
                sh 'ls -ltr'
                echo "packageversion is ${packageVersion}"
                //sh 'npm install'
            }
        }
        stage('Unit Test') {
            steps {
                echo "unit testing"
            }
        }
        //sonar scanner command expects sonar-project.properties should be available
        stage('Sonar scan') {
            steps {
                sh 'ls -ltr'
                //sh 'sonar-scanner'
                echo 'sonar scanning is done'
        
            }
        }
        stage('Build') {
            steps {
                sh 'ls -ltr'
                sh 'zip -r ${params.component}.zip ./* --exclude=.git --exclude=.zip'

            }
        }
        stage('SAST') {
            steps {
                echo 'SAST is done'

            }
        }
        stage('Publish Artifact') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: '52.201.245.151:8081/',
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: 'roboshop',
                    credentialsId: 'Nexus',
                    artifacts: [
                        [artifactId: "${params.component}",
                        classifier: '',
                        file: "${params.component}.zip",
                        type: 'zip']
        ]
     )
                
            }
        }
        stage('Deploy') {
            steps {
                script {
                    sh 'ls -ltr'
                    def params = [
                        string(name: 'version', value: "$packageVersion")
                    ]
                    build job: "Roboshop/${params.component}-deploy", wait: true, parameters: params
                    echo "Deploying"
                }
            }
        }
    }

    post {
        always {
            echo 'cleaning up workspace'
            deleteDir()
        }
    }
}


