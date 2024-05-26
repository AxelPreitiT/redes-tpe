def createJiraIssue(url, key, issueTypeName, creds, title, link, linkTitle) {
    def jiraResponse = sh(script: """curl -D- -u ${creds} -X POST --data '{ "fields": { "project": { "key": "${key}" }, "summary": "${title}", "issuetype": { "name": "${issueTypeName}" } } }' -H 'Content-Type: application/json' ${url}/rest/api/3/issue""", returnStdout: true).trim()

    def jiraIssueIdMatcher = (jiraResponse =~ /"id":"([^"]+)"/)
    def jiraKeyMatcher = (jiraResponse =~ /"key":"([^"]+)"/)
    def jiraSelfMatcher = (jiraResponse =~ /"self":"([^"]+)"/)

    def jiraIssueId = jiraIssueIdMatcher ? jiraIssueIdMatcher[0][1] : null
    def jiraKey = jiraKeyMatcher ? jiraKeyMatcher[0][1] : null
    def jiraSelf = jiraSelfMatcher ? jiraSelfMatcher[0][1] : null

    if (!jiraIssueId || !jiraKey || !jiraSelf) {
        error "Failed to extract necessary fields from Jira response: ${jiraResponse}"
    }
    
    sh(script: """curl -D- -u ${creds} -X POST --data '{ "object": { "url": "${link}", "title": "${linkTitle}" } }' -H 'Content-Type: application/json' ${url}/rest/api/3/issue/${jiraIssueId}/remotelink""")
    
    return [jiraIssueId: jiraIssueId, jiraKey: jiraKey, jiraSelf: jiraSelf]
}

def addJiraComment(comment, jiraKey) {
    jiraComment body: comment, issueKey: jiraKey
}

return this