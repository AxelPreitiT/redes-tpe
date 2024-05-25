def createJiraIssue(url, key, issueTypeName, cred, title, link, linkTitle) {
    def jiraResponse = sh(script: '''curl -D- -u '$cred' -X POST --data '{ \"fields\": { \"project\": { \"key\": \"'$key'\" }, \"summary\": \"'$title'\", \"issuetype\": { \"name\": \"'$issueTypeName'\" } } }' -H 'Content-Type: application/json' '$url'/rest/api/3/issue''', returnStdout: true).trim()
    def jiraIssueId = (jiraResponse =~ /"id":"([^"]+)"/)[0][1]
    def jiraKey = (jiraResponse =~ /"key":"([^"]+)"/)[0][1]
    def jiraSelf = (jiraResponse =~ /"self":"([^"]+)"/)[0][1]
    sh '''curl -D- -u '$cred' -X POST --data '{ \"object\": { \"url\": \"'$link'\", \"title\": \"'$linkTitle'\" } }' -H 'Content-Type: application/json' '$url'/rest/api/3/issue/'$jiraIssueId'/remotelink'''
    return [jiraIssueId: jiraIssueId, jiraKey: jiraKey, jiraSelf: jiraSelf]
}

def addJiraComment(comment, jiraKey) {
    jiraComment body: comment, issueKey: jiraKey
}

return this