def buildResponse
def testResponse
def deployResponse
def deployInput
def developmentOutput
def slackInit
def deployResponse2
def jiraFunctions

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
                    jiraFunctions = load "jira.groovy"
                    slackInit = slackSend(message: "Pipeline for ${env.JOB_name} ${env.BUILD_NUMBER} run. (<${env.BUILD_URL}|Open>).")
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
                    slackSend (channel: slackInit.threadId, message: "Please visit Jenkins to authorize the ${env.JOB_NAME} ${env.BUILD_NUMBER} deployment (<${env.BUILD_URL}input|Open>). The development build is now deployed <${developmentUrl}|here>.")
                    emailext mimeType: 'text/html',
                        subject: "[Jenkins]${currentBuild.fullDisplayName}",
                        to: "gfrancois@itba.edu.ar",
                        body: """
                        <div style="text-align: center;">
                            <img style="display: block; margin-left: auto; margin-right: auto;" src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Jenkins_logo_with_title.svg/2560px-Jenkins_logo_with_title.svg.png" alt="Archivo:Jenkins logo with title.svg - Wikipedia, la enciclopedia libre" width="363" height="117">
                            <br>
                            <span style="font-size: 18pt;">
                                <strong>¡Tienes una nueva aprobación pendiente!</strong>
                            </span>
                            <br>
                            <br>Para abortar o aceptar el deployment ${env.BUILD_NUMBER} por favor haz click en el boton "Aprobar/Abortar" debajo.
                            <br>
                            <br>Si previamente prefieres revisar el build puedes revisarlo mediante el boton "Development build".
                            <br>
                            <br>
                            <br>
                            <div style="display: flex; justify-content: center; gap: 20px;">
                                <a href="${BUILD_URL}input" target="_blank" class="cloudHQ__gmail_elements_final_btn" style="background-color: #d33834; color: #ffffff; border: 4px solid #6d6b6d; border-radius: 15px; box-sizing: border-box; font-size: 13px; font-weight: bold; line-height: 35px; padding: 6px 12px; text-align: center; text-decoration: none; text-transform: uppercase; vertical-align: middle;" rel="noopener">Aprobar/Abortar</a>
                                <a href="${developmentUrl}" target="_blank" class="cloudHQ__gmail_elements_final_btn" style="background-color: #d33834; color: #ffffff; border: 4px solid #6d6b6d; border-radius: 15px; box-sizing: border-box; font-size: 13px; font-weight: bold; line-height: 35px; padding: 6px 12px; text-align: center; text-decoration: none; text-transform: uppercase; vertical-align: middle;" rel="noopener">Development build</a>
                            </div>
                            <br>
                            <br>
                            <br>
                            <br>
                        </div>
                        """
                    input id: 'Approve_deploy', message: "Are you sure you want to deploy the build?", ok: 'Deploy'
                    deployResponse2 = slackSend(channel: slackInit.threadId, message:"Deploying to prod environment")
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
                        deployResponse2.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script {
                        deployResponse.addReaction("x")
                        slackInit.addReaction("x")
                        jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Deploy failed: #'$BUILD_NUMBER'", env.BUILD_URL, "Jenkins build")
                        deployResponse2.addReaction("x")   
                    }
                }
                aborted {
                    script {
                        deployResponse.addReaction("no_entry")
                        slackInit.addReaction("no_entry")  
                        deployResponse2.addReaction("no_entry")     
                    } 
                }
                always {
                    script {
                        slackInit.removeReaction("stopwatch")
                    }
                }
            }            
        }
    }
}