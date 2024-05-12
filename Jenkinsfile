def buildResponse
def testResponse
def deployResponse
pipeline {
    agent any
    tools {
        nodejs "node-20"
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                script{
                    buildResponse = slackSend (message: "Build stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                }
                sh 'npm install'
                sh 'npm run build'
                // emailext(attachLog: true, body: 'Hello Jose', subject: 'This is a test for an email', to: 'jmentasti@itba.edu.ar')
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
            }
            
        }
    }
}