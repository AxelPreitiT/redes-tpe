def buildResponse
def testResponse
def deployResponse
def deployInput
pipeline {
    agent { dockerfile true }
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                script{
                    buildResponse = slackSend (message: "Build stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                }
                sh 'npm install'
                sh 'npm run build'
            }
            post {
                success {
                    script {
                        buildResponse.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script {
                        buildResponse.addReaction("x")    
                    }
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
                echo 'With webhook'
                script{
                    testResponse = slackSend (message: "Test stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                }
                sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                sh 'npm run test'
            }
            post {
                success {
                    script{
                        testResponse.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script{
                        testResponse.addReaction("x")    
                    }
                }
            }
        }
        stage('Deploy') {
            steps{
                echo 'Deploying'
                script{
                    deployResponse = slackSend (message: "Deploy stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                    slackSend (message: "Please check your emails to authorize the deployment ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                    emailext mimeType: 'text/html',
                        subject: "[Jenkins]${currentBuild.fullDisplayName}",
                        to: "jmentasti@itba.edu.ar",
                        body: '''<a href="${BUILD_URL}input">click to review</a>'''
                    input id: 'Approve_deploy', message: 'Are you sure you want to deploy the build?', ok: 'Deploy'
                    echo "Deployed"
                }
            }
            post {
                success {
                    script{
                        deployResponse.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script{
                        deployResponse.addReaction("x")    
                    }
                }
                aborted {
                    script{
                        deployResponse.addReaction("no_entry")    
                    } 
                }
            }
            
        }
    }
}