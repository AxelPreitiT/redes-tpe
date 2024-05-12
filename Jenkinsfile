pipeline {
    agent any
    tools {
        nodejs "node-20"
    }
    stages {
        stage('Build') {
            steps {
                script{
                    echo 'Building'
                    def buildResponse = slackSend (message: "Build stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                    sh 'npm install'
                    sh 'npm run build'
                    buildResponse.addReaction("white_check_mark")
                }
                // emailext(attachLog: true, body: 'Hello Jose', subject: 'This is a test for an email', to: 'jmentasti@itba.edu.ar')
            }
        }
        stage('Test') {
            steps {
                script{
                    echo 'Testing'
                    def testResponse = slackSend (message: "Test stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                    sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                    sh 'npm run test'
                    testResponse.addReaction("white_check_mark")
                }
                
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }
}