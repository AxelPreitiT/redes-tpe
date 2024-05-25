def createJiraIssue(url, key, issueTypeName, cred, title, link, linkTitle) {
    def jiraResponse = sh(script: """curl -D- -u '$cred' -X POST --data '{ "fields": { "project": { "key": "${key}" }, "summary": "${title}", "issuetype": { "name": "${issueTypeName}" } } }' -H 'Content-Type: application/json' ${url}/rest/api/3/issue""", returnStdout: true).trim()

    def jiraIssueIdMatcher = jiraResponse =~ /"id":"([^"]+)"/
    def jiraKeyMatcher = jiraResponse =~ /"key":"([^"]+)"/
    def jiraSelfMatcher = jiraResponse =~ /"self":"([^"]+)"/

    def jiraIssueId = jiraIssueIdMatcher ? jiraIssueIdMatcher[0][1] : null
    def jiraKey = jiraKeyMatcher ? jiraKeyMatcher[0][1] : null
    def jiraSelf = jiraSelfMatcher ? jiraSelfMatcher[0][1] : null

    if (jiraIssueId && jiraKey && jiraSelf) {
        sh(script: """curl -D- -u '$cred' -X POST --data '{ "object": { "url": "${link}", "title": "${linkTitle}" } }' -H 'Content-Type: application/json' ${url}/rest/api/3/issue/${jiraIssueId}/remotelink""")
    } else {
        error "Failed to extract necessary fields from Jira response: ${jiraResponse}"
    }

    return [jiraIssueId: jiraIssueId, jiraKey: jiraKey, jiraSelf: jiraSelf]
}

def addJiraComment(comment, jiraKey) {
    jiraComment body: comment, issueKey: jiraKey
}

return this