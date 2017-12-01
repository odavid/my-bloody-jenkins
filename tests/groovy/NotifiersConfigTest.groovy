import org.yaml.snakeyaml.Yaml
import jenkins.plugins.hipchat.model.notifications.Notification.Color


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

def testHipchat(){
 	def config = new Yaml().load("""
hipchat:
  server: https://hipchat.domain.com
  room: hipchatRoom
  sendAs: hipchatJenkins
  v2Enabled: true
  credentialId: hipchat-token
  defaultNotifications:
    - notifyEnabled: true
      textFormat: true
      notificationType: FAILURE
      color: RED
      messageTemplate: mmm##yy
""")
    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(jenkins.plugins.hipchat.HipChatNotifier)
    assert desc.server == 'https://hipchat.domain.com'
    assert desc.room == 'hipchatRoom'
    assert desc.sendAs == 'hipchatJenkins'
    assert desc.v2Enabled
    assert desc.credentialId == 'hipchat-token'
    
    def defaultNotification = desc.defaultNotifications[0]
    
    assert defaultNotification instanceof jenkins.plugins.hipchat.model.NotificationConfig
    assert defaultNotification.notifyEnabled
    assert defaultNotification.textFormat
    assert defaultNotification.notificationType == jenkins.plugins.hipchat.model.NotificationType.FAILURE
    assert defaultNotification.color == Color.RED
    assert defaultNotification.messageTemplate == 'mmm##yy'
}

def testNotifiers(){
    testSlack()
    testHipchat()
}

testNotifiers()