def buildResponse
def testResponse
def deployResponse
def deployInput
def developmentOutput
def slackInit
def deployResponse2
def jiraFunctions
def mailFunctions
def deployFunctions

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
        AZURE_CRED = credentials('azure-jose')
    }
    stages {
        stage('Build') {
            steps {
                script{
                    jiraFunctions = load "jira.groovy"
                    mailFunctions = load "mail.groovy"
                    deployFunctions = load "deploy.groovy"
                    slackInit = slackSend(message: "Pipeline for ${env.JOB_name} <${env.BUILD_URL}|#${env.BUILD_NUMBER}> started")
                    slackInit.addReaction("stopwatch")
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
                        slackInit.removeReaction("stopwatch")
                        slackInit.addReaction("x")
                        jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Build failed: #'$BUILD_NUMBER'", env.BUILD_URL, "Jenkins build")
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
                        slackInit.removeReaction("stopwatch")
                        slackInit.addReaction("x")
                        jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Test failed: #'$BUILD_NUMBER'", env.BUILD_URL, "Jenkins build")
                    }
                }
                always{
                    junit testResults: 'junit.xml'
                }
            }
        }
        stage('Dev deploy') {
            steps {
                echo 'Deploying dev'
                script {
                    deployResponse = slackSend (channel: slackInit.threadId, message: "Dev deploy stage started")
                    deployResponse.addReaction("stopwatch")
                    withEnv(['RESOURCE_GROUP_NAME=redes-jenkins-development_group',
                            'WEB_APP_NAME=redes-jenkins-development']) {
                        developmentOutput = deployFunctions.deploy(RESOURCE_GROUP_NAME, WEB_APP_NAME, AZURE_CRED_USR, AZURE_CRED_PSW)    
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
                    slackSend (channel: slackInit.threadId, message: "Please visit Jenkins to authorize the ${env.JOB_NAME} <${env.BUILD_URL}input|#${env.BUILD_NUMBER}> production deployment. The development build is now deployed <${developmentUrl}|here>.")
                    mailFunctions.sendEmail(developmentUrl, "${BUILD_URL}input", env.BUILD_NUMBER, "[Jenkins] Pipeline #${env.BUILD_NUMBER}") 
                    input id: 'Approve_deploy', message: "Are you sure you want to deploy the build?", ok: 'Deploy'
                }
            }
            post {
                success {
                    script {
                        deployResponse.addReaction("white_check_mark")     
                    }
                }
                failure {
                    script {
                        slackInit.removeReaction("stopwatch")
                        slackInit.addReaction("x")
                        deployResponse.addReaction("x")
                        jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Dev deploy failed: #'$BUILD_NUMBER'", env.BUILD_URL, "Jenkins build")
                    }
                }
                aborted {
                    script {
                        slackInit.removeReaction("stopwatch")
                        deployResponse.addReaction("no_entry")
                        slackInit.addReaction("no_entry")      
                    } 
                }
                always {
                    script {
                        deployResponse.removeReaction("stopwatch")
                    }
                }
            }
        }
        stage('Prod deploy') {
            steps {
                echo 'Deploying prod'
                script {
                    deployResponse2 = slackSend (channel: slackInit.threadId, message: "Prod deploy stage started")
                    deployResponse2.addReaction("stopwatch")
                    withEnv(['RESOURCE_GROUP_NAME=Jenkins-Deployment',
                            'WEB_APP_NAME=redes-jenkins-deploy']) {
                        deployFunctions.deploy(RESOURCE_GROUP_NAME, WEB_APP_NAME, AZURE_CRED_USR, AZURE_CRED_PSW)
                    }
                    echo "Deployed"
                }
            }
            post {
                success {
                    script {
                        slackInit.addReaction("white_check_mark")    
                        deployResponse2.addReaction("white_check_mark")     
                    }
                }
                failure {
                    script {
                        slackInit.addReaction("x")
                        deployResponse2.addReaction("x")
                        jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Prod deploy failed: #'$BUILD_NUMBER'", env.BUILD_URL, "Jenkins build")   
                    }
                }
                aborted {
                    script {
                        slackInit.addReaction("no_entry") 
                        deployResponse2.addReaction("no_entry")     
                    } 
                }
                always {
                    script {
                        slackInit.removeReaction("stopwatch")
                        deployResponse2.removeReaction("stopwatch")
                    }
                }
            }            
        }
    }
}