import org.yaml.snakeyaml.Yaml

handler = 'JiraSteps'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testJiraSteps(){
    def config = new Yaml().load("""
sites:
  - name: JIRA
    url: https://jira.domain.com
    loginType: OAUTH
    secret: secret
    token: token
    timeout: 10
    readTimeout: 20
    userName: jira
    password: jira

""")

    configHandler.setup(config)
    def desc = org.thoughtslive.jenkins.plugins.jira.Config.DESCRIPTOR
    def site = desc.sites[0]
    assert site.name == ('JIRA')
    assert site.url == (new URL('https://jira.domain.com'))
    assert site.loginType == 'OAUTH'
    assert site.secret.toString() == 'secret'
    assert site.token.toString() == 'token'
    assert site.timeout == 10
    assert site.readTimeout == 20
    assert site.userName == 'jira'
    assert site.password.toString() == 'jira'
}
testJiraSteps()
