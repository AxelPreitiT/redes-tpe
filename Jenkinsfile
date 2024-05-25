def buildResponse
def testResponse
def deployResponse
def deployInput
def developmentOutput
def slackInit

pipeline {
    agent { 
        dockerfile {
            filename 'Dockerfile'
        }
    }
    environment {
        JIRA_URL = 'https://redesjenkins.atlassian.net'
        JIRA_KEY = 'KAN'
        JIRA_ISSUE_TYPE_NAME = 'JenkinsError'
        JIRA_CRED = credentials('jira-token')
    }
    stages {
        stage('Build') {
            steps {
                script{
                    slackInit = slackSend(message: "Pipeline for ${env.JOB_name} ${env.BUILD_NUMBER} run. (<${env.BUILD_URL}|Open>).")
                }
                echo 'Building'
                script {
                    buildResponse = slackSend (channel: slackInit.threadId, message: "Build stage started")
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
                        slackInit.addReaction("x")
                        sh '''curl -D- -u $JIRA_CRED -X POST --data '{ \"fields\": { \"project\": { \"key\": \"'$JIRA_KEY'\" }, \"summary\": \"Build failed: #'$BUILD_NUMBER'\", \"issuetype\": { \"name\": \"'$JIRA_ISSUE_TYPE_NAME'\" } } }' -H 'Content-Type: application/json' $JIRA_URL/rest/api/3/issue'''   
                    }
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Testing'
                echo 'With webhook'
                script{
                    testResponse = slackSend (channel: slackInit.threadId, message: "Test stage started")
                }
                sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                sh 'npm run test'
            }
            post {
                success {
                    script {
                        testResponse.addReaction("white_check_mark")
                    }
                }
                failure {
                    script {
                        testResponse.addReaction("x") 
                        slackInit.addReaction("x")
                        sh '''curl -D- -u $JIRA_CRED -X POST --data '{ \"fields\": { \"project\": { \"key\": \"'$JIRA_KEY'\" }, \"summary\": \"Test failed: #'$BUILD_NUMBER'\", \"issuetype\": { \"name\": \"'$JIRA_ISSUE_TYPE_NAME'\" } } }' -H 'Content-Type: application/json' $JIRA_URL/rest/api/3/issue'''      
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
                script {
                    deployResponse = slackSend (channel: slackInit.threadId, message: "Deploy stage started")
                    withCredentials([usernamePassword(credentialsId: 'azure-jose', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
                            sh 'az login -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET > /dev/null'
                    }
                    sh 'npm run build'
                    sh 'cp -r .next/static .next/standalone/.next/static'
                    sh 'cp -r public .next/standalone/public'
                    sh 'zip deploy .next -qr'
                    withEnv(['RESOURCE_GROUP_NAME=redes-jenkins-development_group',
                            'WEB_APP_NAME=redes-jenkins-development']) {
                        developmentOutput = sh script: 'az webapp deploy --resource-group $RESOURCE_GROUP_NAME --name $WEB_APP_NAME --src-path deploy.zip --type zip --clean true 2>&1', returnStdout: true
                    }
                    echo "Command Output: ${developmentOutput}"
                    def urlLine = developmentOutput.readLines().find { it.contains("WARNING: You can visit your app at:") }
                    def developmentUrl = ""
                     if (urlLine) {
                        def urlMatcher = (urlLine =~ /WARNING: You can visit your app at: (http:\/\/[^\s]+)/)
                        if (urlMatcher.find()) {
                            developmentUrl = urlMatcher.group(1)
                        }
                    }
                    echo "Development URL: ${developmentUrl}"
                    slackSend (message: "Please visit Jenkins to authorize the ${env.JOB_NAME} ${env.BUILD_NUMBER} deployment (<${env.BUILD_URL}input|Open>). The development build is now deployed <${developmentUrl}|here>.")
                    emailext mimeType: 'text/html',
                        subject: "[Jenkins]${currentBuild.fullDisplayName}",
                        to: "jmentasti@itba.edu.ar",
                        body: """<a href="${BUILD_URL}input">Click to review</a>. Test it on <a href="${developmentUrl}">the dev branch</a>."""
                    input id: 'Approve_deploy', message: "Are you sure you want to deploy the build?", ok: 'Deploy'
                    withEnv(['RESOURCE_GROUP_NAME=Jenkins-Deployment',
                            'WEB_APP_NAME=redes-jenkins-deploy']) {
                        sh 'az webapp deploy --resource-group $RESOURCE_GROUP_NAME --name $WEB_APP_NAME --src-path deploy.zip --type zip --clean true'
                    }
                    echo "Deployed"
                }
            }
            post {
                success {
                    script {
                        slackInit.addReaction("white_check_mark")    
                        deployResponse.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script {
                        deployResponse.addReaction("x")
                        slackInit.addReaction("x")
                        sh '''curl -D- -u $JIRA_CRED -X POST --data '{ \"fields\": { \"project\": { \"key\": \"'$JIRA_KEY'\" }, \"summary\": \"Deploy failed: #'$BUILD_NUMBER'\", \"issuetype\": { \"name\": \"'$JIRA_ISSUE_TYPE_NAME'\" } } }' -H 'Content-Type: application/json' $JIRA_URL/rest/api/3/issue'''    
                    }
                }
                aborted {
                    script {
                        deployResponse.addReaction("no_entry")
                        slackInit.addReaction("no_entry")    
                    } 
                }
            }            
        }
    }
}