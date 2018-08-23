import org.yaml.snakeyaml.Yaml

handler = 'Jira'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

credsHandlerName = 'Creds'
credsHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${credsHandlerName}Config.groovy"))

def testJiraWithUserPassword(){
 	def config = new Yaml().load("""
sites:
  - url: https://jira.domain.com
    alternativeUrl: https://jira-alt.domain.com
    username: jira
    password: jira
    supportsWikiStyleComment: true
    recordScmChanges: true
    userPattern: user(.*)xxx
    updateJiraIssueForAllStatus: true
    groupVisibility: visible
    roleVisibility: visible
    useHTTPAuth: true
    disableChangelogAnnotations: true
    timeout: 20
    dateTimePattern: '%MM:%%SS'
    appendChangeTimestamp: true

""")

    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.jira.JiraProjectProperty)
    def site = desc.sites[0]
    // It is private, but I want to see it has value
    def credIdField = hudson.plugins.jira.JiraSite.getDeclaredField('credentialsId')
    credIdField.setAccessible(true)

    assert site.url == (new URL('https://jira.domain.com/'))
    assert site.getAlternativeUrl('xxx') == (new URL('https://jira-alt.domain.com/browse/XXX'))
    assert credIdField.get(site)
    assert site.supportsWikiStyleComment
    assert site.recordScmChanges
    assert site.userPattern.toString() == 'user(.*)xxx'
    assert site.updateJiraIssueForAllStatus
    assert site.groupVisibility == 'visible'
    assert site.roleVisibility == 'visible'
    assert site.useHTTPAuth
    assert site.disableChangelogAnnotations
    assert site.timeout == 20
    assert site.dateTimePattern == '%MM:%%SS'
    assert site.appendChangeTimestamp
}

def testJiraWithCredentialsId(){
    def credsConfig = new Yaml().load("""
XXX:
  type: userpass
  username: user
  password: password1234
  description: userpass description
""")
    credsHandler.setup(credsConfig)

 	def config = new Yaml().load("""
sites:
  - url: https://jira.domain.com
    alternativeUrl: https://jira-alt.domain.com
    credentialsId: XXX
    supportsWikiStyleComment: true
    recordScmChanges: true
    userPattern: user(.*)xxx
    updateJiraIssueForAllStatus: true
    groupVisibility: visible
    roleVisibility: visible
    useHTTPAuth: true
    disableChangelogAnnotations: true
    timeout: 20
    dateTimePattern: '%MM:%%SS'
    appendChangeTimestamp: true

""")

    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.jira.JiraProjectProperty)
    def site = desc.sites[0]
    // It is private, but I want to see it has value
    def credIdField = hudson.plugins.jira.JiraSite.getDeclaredField('credentialsId')
    credIdField.setAccessible(true)

    assert site.url == (new URL('https://jira.domain.com/'))
    assert site.getAlternativeUrl('xxx') == (new URL('https://jira-alt.domain.com/browse/XXX'))
    assert credIdField.get(site)
    assert site.supportsWikiStyleComment
    assert site.recordScmChanges
    assert site.userPattern.toString() == 'user(.*)xxx'
    assert site.updateJiraIssueForAllStatus
    assert site.groupVisibility == 'visible'
    assert site.roleVisibility == 'visible'
    assert site.useHTTPAuth
    assert site.disableChangelogAnnotations
    assert site.timeout == 20
    assert site.dateTimePattern == '%MM:%%SS'
    assert site.appendChangeTimestamp
}

testJiraWithUserPassword()
testJiraWithCredentialsId()