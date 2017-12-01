import org.yaml.snakeyaml.Yaml

handler = 'Notifiers'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testSlack(){
 	def config = new Yaml().load("""
slack:
  teamDomain: slack
  credentialId: slack-token
  botUser: true
  room: channel1,channel2
  baseUrl: https://slack.domain.com
  sendAs: slackJenkins
  
""")
    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(jenkins.plugins.slack.SlackNotifier)
    assert desc.tokenCredentialId == 'slack-token'
    assert desc.teamDomain == 'slack'
    assert desc.botUser
    assert desc.room == 'channel1,channel2'
    assert desc.baseUrl == 'https://slack.domain.com/'
    assert desc.sendAs == 'slackJenkins'
}

def testNotifiers(){
    testSlack()
}

testNotifiers()