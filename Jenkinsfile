pipeline {
    agent any
    tools {
        nodejs "node-20"
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                sh 'npm install'
                sh 'npm run build'
                // emailext(attachLog: true, body: 'Hello Jose', subject: 'This is a test for an email', to: 'jmentasti@itba.edu.ar')
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
                sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                sh 'npm run test'
                sh 'kill "$api_pid"'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }
}