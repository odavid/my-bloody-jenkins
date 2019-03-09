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
    assert desc.baseUrl == 'https://slack.domain.com'
    assert desc.sendAs == 'slackJenkins'
}


def testMail(){
 	def config = new Yaml().load("""
mail:
  host: mail.domain.com
  port: 265
  defaultSuffix: '@domain.com'
  charset: 'UTF-8'
  defaultContentType: text/html
  defaultSubject: 'You broke the build'
  defaultBody: 'You really broke the build'
  emergencyReroute: 'emergency@domain.com'
  replyToAddress: 'jenkins.admin@domain.com'
  defaultPresendScript: 'println "aaa"'
  defaultPostsendScript: 'println "aaa"'
  maxAttachmentSize: 50
  recipientList:
    - kuku@domain.com
    - muku@domain.com
  excludedCommitters:
    - joe
    - david
  listId: mailing-list
  requireAdminForTemplateTesting: true
  enableWatching: true
  authUser: mail-admin
  authPassword: password
  useSsl: true
  debugMode: true
""")
    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.emailext.ExtendedEmailPublisher)
    assert desc.smtpServer == 'mail.domain.com'
    assert desc.smtpPort == '265'
    assert desc.defaultSuffix == '@domain.com'
    assert desc.charset == 'UTF-8'
    assert desc.defaultContentType == 'text/html'
    assert desc.defaultSubject == 'You broke the build'
    assert desc.defaultBody == 'You really broke the build'
    assert desc.emergencyReroute == 'emergency@domain.com'
    assert desc.defaultReplyTo == 'jenkins.admin@domain.com'
    assert desc.defaultPresendScript == 'println "aaa"'
    assert desc.defaultPostsendScript == 'println "aaa"'
    assert desc.maxAttachmentSizeMb == 50
    assert desc.recipientList == 'kuku@domain.com,muku@domain.com'
    assert desc.excludedCommitters == 'joe,david'
    assert desc.listId == 'mailing-list'
    assert desc.adminRequiredForTemplateTesting
    assert desc.watchingEnabled
    assert desc.smtpUsername == 'mail-admin'
    assert desc.smtpPassword.toString() == 'password'
    assert desc.useSsl
    assert desc.debugMode

    desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.tasks.Mailer)
    assert desc.useSsl
    assert desc.smtpAuthUsername == 'mail-admin'
    assert desc.smtpAuthPassword.toString() == 'password'
    assert desc.charset == 'UTF-8'
    assert desc.smtpServer == 'mail.domain.com'
    assert desc.smtpPort == '265'
    assert desc.replyToAddress == 'jenkins.admin@domain.com'

}

def testNotifiers(){
    testSlack()
    testMail()
}

testNotifiers()