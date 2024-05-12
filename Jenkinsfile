pipeline {
    agent any
    tools {
        nodejs "node-20"
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                def buildResponse = slackSend (message: "Start Build stage finished for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                sh 'npm install'
                sh 'npm run build'
                buildResponse.addReaction("thumbsup")
                // emailext(attachLog: true, body: 'Hello Jose', subject: 'This is a test for an email', to: 'jmentasti@itba.edu.ar')
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
                def testResponse = slackSend (message: "Test stage finished for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
                sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                sh 'npm run test'
                testResponse.addReaction("thumbsup")
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }
}