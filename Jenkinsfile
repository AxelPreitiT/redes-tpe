def buildResponse
def testResponse
def deployResponse
def deployInput
def developmentOutput
pipeline {
    agent { 
        dockerfile {
            filename 'Dockerfile'
        }
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building'
                script {
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
                    script {
                        testResponse.addReaction("white_check_mark")
                        withCredentials([usernamePassword(credentialsId: 'jira-token', passwordVariable: 'JIRA_API_TOKEN', usernameVariable: 'JIRA_EMAIL')]) {
                            sh 'curl --request POST --url "https://redesjenkins.atlassian.net/rest/api/3/issue" \
                                --user "$JIRA_EMAIL:$JIRA_API_TOKEN"  --header "Accept: application/json" --header "Content-Type: application/json" \
                                --data """{
                                    "fields": {
                                        "project": {
                                            "key": "RED"
                                        },
                                        "summary": "Test passed",
                                        "description": "The test stage passed successfully",
                                        "issuetype": {
                                            "name": "Task"
                                        }
                                    }
                                }"""'
                        }
                    }
                }
                failure {
                    script {
                        testResponse.addReaction("x")    
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying'
                script {
                    deployResponse = slackSend (message: "Deploy stage started for ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)")
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
                        deployResponse.addReaction("white_check_mark")       
                    }
                }
                failure {
                    script {
                        deployResponse.addReaction("x")    
                    }
                }
                aborted {
                    script {
                        deployResponse.addReaction("no_entry")    
                    } 
                }
            }            
        }
    }
}