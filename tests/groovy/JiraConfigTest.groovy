import org.yaml.snakeyaml.Yaml

handler = 'Jira'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testJira(){
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
    assert site.url == (new URL('https://jira.domain.com/'))
    assert site.getAlternativeUrl('xxx') == (new URL('https://jira-alt.domain.com/browse/XXX'))
    assert site.userName == 'jira'
    assert site.password.toString() == 'jira'
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

testJira()