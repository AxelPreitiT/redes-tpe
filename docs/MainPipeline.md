# Configuración Jenkins

```Groovy
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
            args '-u root:root'
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
```

## Agent

Mediante la sección agent lo que se determina es el nodo/ambiente en el cual se va a correr el proceso Jenkins. En este caso, se determina que el Pipeline utilizará el contenedor Docker con las configuraciones en el archivo ``/Dockerfile``

```Groovy
agent { 
    dockerfile {
        filename 'Dockerfile'
    }
}
```

## Stages

La sección Stages engloba todos los Stage, o pasos, que se realizarán durante el Pipeline.

### Build 

Durante este Step se buildea el proyecto, donde en primera medida se envía un mensaje al workspace en Slack dentro del hilo correspondiente, para luego efectivamente levantar el proyecto con los comandos __npm__.

Finalmente en caso de ser un buid exitoso se reacciona el mensaje anterior con un emoji distintivo (✅) y mismo caso para errores en el build (❌) donde a su vez agrega un nuevo issue en Jira con el nombre: Build failed: #\<Numero de pipeline\> 

```Groovy
stage('Build') {
    steps {
        script{
            jiraFunctions = load "jira.groovy"
            mailFunctions = load "mail.groovy"
            deployFunctions = load "deploy.groovy"
            slackInit = slackSend(message: "Pipeline for ${env.JOB_name} <${env.BUILD_URL}|#${env.BUILD_NUMBER}> started")
            slackInit.addReaction("stopwatch")
            buildResponse = slackSend (channel: slackInit.threadId  message: "Build stage started")
        }
        echo 'Building'
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
```


### Test

Durante este Step se corren los test del proyecto, donde en primera medida se envía un mensaje al workspace en Slack dentro del hilo correspondiente, para luego efectivamente correr los test del proyecto con los comandos __npm__. Vale mencionar que para poder correr el servicio y los test, se genera un proceso en background y se guarda el PID asociado.

Finalmente en caso de cumplir todos los test se reacciona el mensaje anterior con un emoji distintivo (✅) o  en caso de errores se reacciona con otro emoji (❌) y se crea un nuevo issue en Jira con el nombre Test failed: #\<Numero de pipeline\>


```Groovy
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
```

### Deploy Dev

En este tercer Step se realiza el deploy a nivel de dessarrollo del proyecto. Para tales motivos se cuenta con una máquina virtual Azure con credenciales resguardadas en un archivo ``.env``

En cuanto al proceso secuencial, se comienza por mandar un mensaje al workpace en Slack dentro del hilo correspondiente, para luego correr la función auxiliar __deployFunctions.deploy__ para autentificarse en la máquina virtual con las credenciales mencionadas y una vez autenticado, se buildea el proyecto. Finalmente, se detalla el URL con el cual uno puede acceder al servicio y se envía un mensaje de Slack al hilo correspondiente y un mail pidiendo aprobación para realizar el deploy a nivel de producción.

Finalmente al igual que todos los pasos anteriores, en función del estado general del deploy se reacciona al mensaje con un emoji distintivo, ✅ para casos de éxito, ❌ en caso de ocurrir algún error y 🚫 en caso de haber sido abortado. Asi como también se crea un nuevo issue en Jira  con el nombre "Dev deploy failed #\<Numero de pipeline\>" en caso de haber ocurrido algun error en el deploy.

```Groovy
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
```


### Deploy Prod

En este Step se realiza el deploy a nivel de producción del proyecto. Para tales motivos se cuenta con una máquina virtual Azure con credenciales resguardadas en un archivo ``.env``

En cuanto al proceso secuencial, se comienza por mandar un mensaje al workpace en Slack dentro del hilo correspondiente, para luego correr la función auxiliar __deployFunctions.deploy__ para autentificarse en la máquina virtual con las credenciales mencionadas y una vez autenticado, se buildea el proyecto. Luego, se hace el deploy a producción. 

Finalmente al igual que todos los pasos anteriores, en función del estado general del deploy se reacciona al mensaje con un emoji distintivo, ✅ para casos de éxito, ❌ en caso de ocurrir algún error y 🚫 en caso de haber sido abortado. Asi como también se crea un nuevo issue en Jira  con el nombre "Prod deploy failed #\<Numero de pipeline\>" en caso de haber ocurrido algun error en el deploy.

```Groovy
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
```