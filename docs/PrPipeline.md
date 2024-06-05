
# Configuración Jenkins

```Groovy
def slackInit
def slackBuild
def slackTest
def jiraFunctions
def jiraIssueId
def jiraKey
def jiraSelf

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
        JIRA_ISSUE_TYPE_NAME = 'JenkinsPR'
        JIRA_CRED = credentials('jira-token')
        REPO_PR_URL = 'https://github.com/AxelPreitiT/redes-tpe/pull/'
    }
    stages {
        stage('Build') {
            steps {
                script {
                    jiraFunctions = load "jira.groovy"
                    slackInit = slackSend(message: "Pipeline for Pull Request #$CHANGE_ID. Review it on <$REPO_PR_URL$CHANGE_ID|GitHub>.")
                    slackInit.addReaction("stopwatch")
                    slackBuild = slackSend(channel: slackInit.threadId, message: "Build stage started")
                    def jiraIssueDetails = jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Pull Request opened: #$CHANGE_ID", "$REPO_PR_URL$CHANGE_ID", "Pull Request #$CHANGE_ID")
                    (jiraIssueId, jiraKey, jiraSelf) = [jiraIssueDetails.jiraIssueId, jiraIssueDetails.jiraKey, jiraIssueDetails.jiraSelf]
                }
                echo 'Building'
                sh 'npm install'
                sh 'npm run build'
            }
            post{
                success{
                    script {
                        jiraFunctions.commentJiraIssue("Build success", jiraKey)
                        slackBuild.addReaction("white_check_mark")
                    }
                }
                failure{
                    script {
                        jiraFunctions.commentJiraIssue("Build failure", jiraKey)
                        slackInit.removeReaction("stopwatch")
                        slackBuild.addReaction("x")
                        slackInit.addReaction("x")
                    }
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    slackTest = slackSend(channel: slackInit.threadId, message: "Test stage started")
                }
                echo 'Testing'
                echo 'With webhook'
                sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
                sh 'npm run test'
            }
            post {
                success {
                    script {
                        jiraFunctions.commentJiraIssue("Test success", jiraKey)
                        slackTest.addReaction("white_check_mark")
                        slackInit.addReaction("white_check_mark")
                    }
                }
                failure {
                    script {
                        jiraFunctions.commentJiraIssue("Test failure", jiraKey)
                        slackTest.addReaction("x")
                        slackInit.addReaction("x")
                    }
                }
                always {
                    script {
                        junit testResults: 'junit.xml'
                        slackInit.removeReaction("stopwatch")
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

Durante este Step se buildea el proyecto, donde en primera medida se envía un mensaje al workspace en Slack dentro del hilo correspondiente y se crea un nuevo issue en Jira con el nombre "Pull Request opened: #\<numero del pipeline\>", para luego efectivamente levantar el proyecto con los comandos __npm__.

Finalmente en caso de ser un buid exitoso se reacciona el mensaje anterior con un emoji distintivo (✅) y mismo caso para errores en el build (❌). A su vez, se agrega un comentario al issue creado mencionando si el build fue exitoso o no. 

```Groovy
stage('Build') {
    steps {
        script {
            jiraFunctions = load "jira.groovy"
            slackInit = slackSend(message: "Pipeline for Pull Request #$CHANGE_ID. Review it on <$REPO_PR_URL$CHANGE_ID|GitHub>.")
            slackInit.addReaction("stopwatch")
            slackBuild = slackSend(channel: slackInit.threadId, message: "Build stage started")
            def jiraIssueDetails = jiraFunctions.createJiraIssue(JIRA_URL, JIRA_KEY, JIRA_ISSUE_TYPE_NAME, JIRA_CRED, "Pull Request opened: #$CHANGE_ID", "$REPO_PR_URL$CHANGE_ID", "Pull Request #$CHANGE_ID")
            (jiraIssueId, jiraKey, jiraSelf) = [jiraIssueDetails.jiraIssueId, jiraIssueDetails.jiraKey, jiraIssueDetails.jiraSelf]
        }
        echo 'Building'
        sh 'npm install'
        sh 'npm run build'
    }
    post{
        success{
            script {
                jiraFunctions.commentJiraIssue("Build success", jiraKey)
                slackBuild.addReaction("white_check_mark")
            }
        }
        failure{
            script {
                jiraFunctions.commentJiraIssue("Build failure", jiraKey)
                slackInit.removeReaction("stopwatch")
                slackBuild.addReaction("x")
                slackInit.addReaction("x")
            }
        }
    }
} 
```


### Test

Durante este Step se corren los test del proyecto, donde en primera medida se envía un mensaje al workspace en Slack dentro del hilo correspondiente, para luego efectivamente correr los test del proyecto con los comandos __npm__. Vale mencionar que para poder correr el servicio y los test, se genera un proceso en background y se guarda el PID asociado.

Finalmente en caso de cumplir todos los test se reacciona el mensaje anterior con un emoji distintivo (✅) o  en caso de errores se reacciona con otro emoji (❌). A su vez, se agrega un comentario al issue creado mencionando si el build fue exitoso o no. 

```Groovy
stage('Test') {
    steps {
        script {
            slackTest = slackSend(channel: slackInit.threadId, message: "Test stage started")
        }
        echo 'Testing'
        echo 'With webhook'
        sh 'npm run dev > /dev/null 2>&1 & api_pid=$!'
        sh 'npm run test'
    }
    post {
        success {
            script {
                jiraFunctions.commentJiraIssue("Test success", jiraKey)
                slackTest.addReaction("white_check_mark")
                slackInit.addReaction("white_check_mark")
            }
        }
        failure {
            script {
                jiraFunctions.commentJiraIssue("Test failure", jiraKey)
                slackTest.addReaction("x")
                slackInit.addReaction("x")
            }
        }
        always {
            script {
                junit testResults: 'junit.xml'
                slackInit.removeReaction("stopwatch")
            }
        }
    }
}
```
