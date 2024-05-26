def createJiraIssue(url, key, issueTypeName, creds, title, link, linkTitle) {
    def jiraResponse = sh(script: 'curl -D- -u ' + creds + ' -X POST --data \'{ "fields": { "project": { "key": "' + key + '"}, "summary": "' + title + '", "issuetype": { "name": "' + issueTypeName + '" } } }\' -H \'Content-Type: application/json\' ' + url + '/rest/api/3/issue', returnStdout: true).trim()

    def jiraIssueId = (jiraResponse =~ /"id":"([^\s"]+)"/)[0][1]
    def jiraKey = (jiraResponse =~ /"key":"([^\s"]+)"/)[0][1]
    def jiraSelf = (jiraResponse =~ /"self":"([^\s"]+)"/)[0][1]

    if (!jiraIssueId || !jiraKey || !jiraSelf) {
        error "Failed to extract necessary fields from Jira response: ${jiraResponse}"
    }
    
    sh(script: 'curl -D- -u ' + creds + ' -X POST --data \'{ "object": { "url": "' + link + '", "title": "' + linkTitle + '" } }\' -H \'Content-Type: application/json\' ' + url + '/rest/api/3/issue/' + jiraIssueId + '/remotelink')
    
    return [jiraIssueId: jiraIssueId, jiraKey: jiraKey, jiraSelf: jiraSelf]
}

def commentJiraIssue(comment, jiraKey) {
    jiraComment body: comment, issueKey: jiraKey
}


return this