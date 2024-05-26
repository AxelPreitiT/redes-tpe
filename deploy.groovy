def deploy(resourceGroupName, webAppName, azureUsername, azurePassword){
    sh 'az login -u ' + azureUsername + ' -p ' + $azurePassword ' > /dev/null'\
    sh 'npm run build'
    sh 'cp -r .next/static .next/standalone/.next/static'
    sh 'cp -r public .next/standalone/public'
    sh 'zip deploy .next -qr'
    output = sh script: 'az webapp deploy --resource-group ' + resourceGroupName + ' --name '+ webAppName +' --src-path deploy.zip --type zip --clean true', returnStdout: true
    return output
}

return this