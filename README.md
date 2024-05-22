# Configuración Jenkins

```Groovy
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
```

## Agent

Mediante la sección agent lo que se determina es el nodo/ambiente en el cual se va a correr el proceso Jenkins.

```Groovy
agent { 
    dockerfile {
        filename 'Dockerfile'
    }
}
```

De esta forma se determina que el Pipeline utilizará el contenedor Docker con las configuraciones en el archivo ``/Dockerfile``

## Stages

La sección Stages engloba todos los Stage, o pasos, que se realizarán durante el Pipeline.

### Build 

```Groovy
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
```

Durante este Step se buildea el proyecto, donde en primera medida se envía un mensaje al workspace en Slack correspondiente con las variables de entorno asociadas, para luego efectivamente levantar el proyecto con los comandos __npm__.

Finalmente en caso de ser un buid positivo se reacciona el mensaje anterior con un emoji distintivo (✅) y mismo caso para errores en el build (❌)


### Test

```Groovy
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
            }
        }
        failure {
            script {
                testResponse.addReaction("x")    
            }
        }
    }
}
```

Durante este Step se corren los test del proyecto, donde en primera medida se envía un mensaje al workspace en Slack correspondiente con las variables de entorno asociadas, para luego efectivamente correr los test del proyecto con los comandos __npm__. Vale mencionar que para poder correr el servicio y los test, se genera un proceso en background y se guarda el PID asociado.

Finalmente en caso de cumplir todos los test se reacciona el mensaje anterior con un emoji distintivo (✅) o (❌) para errores.


### Deploy

```Groovy
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
```

En este tercer Step se realiza el deploy del proyecto a un service público. Para tales motivos se cuenta con una máquina virtual Azure con credenciales resguardadas en un archivo ``.env``

En cuanto al proceso secuencial, se comienza por mandar un mensaje al workpace en Slack como en los demás steps, para luego autentificarse en la máquina virtual con las credenciales mencionadas. Una vez autenticado, se buildea el proyecto y con el comando __az webapp deploy__  dentro de withEnv se realiza un deploy a nivel de development. Una vez realizado, se detalla el URL con el cual uno puede acceder al servicio y se envía un mensaje de Slack y un mail pidiendo aprobación para realizar el deploy a nivel de producción. En caso de ser aprobada, nuevamente con el comando __az webapp deploy__ dentro de withEnv se realiza el deploy a nivel de producción.

Finalmente al igual que todos los pasos anteriores, en función del estado general del deploy se reacciona al mensaje inicial con un emoji distintivo, ✅ para casos de éxito, ❌ en caso de ocurrir algún error y 🚫 en caso de haber sido abortado.