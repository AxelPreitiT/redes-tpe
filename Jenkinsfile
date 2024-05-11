pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                emailext(attachLog: true, body: 'Hello Jose', subject: 'This is a test for an email', to: 'jmentasti@itba.edu.ar')
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }
}