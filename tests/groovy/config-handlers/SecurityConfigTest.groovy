import org.yaml.snakeyaml.Yaml

handler = 'Security'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testGoogleLogin(){
	def config = new Yaml().load("""
realmConfig:
  clientId: client-id
  clientSecret: client-secret
  domain: domain.com
""")
    def googleOAuth2Realm = configHandler.setupGoogleOAuth2(config)
    assert googleOAuth2Realm instanceof org.jenkinsci.plugins.googlelogin.GoogleOAuth2SecurityRealm
    assert googleOAuth2Realm.clientId == "client-id"
    assert googleOAuth2Realm.clientSecret.toString() == "client-secret"
    assert googleOAuth2Realm.domain == "domain.com"
}

def testSaml(){
	def config = new Yaml().load("""
realmConfig:
  idpMetadataConfiguration:
    xml: |-
      <xml></xml>
    url: http://xxx.yyy
    period: 10
  displayNameAttributeName: displayName
  groupsAttributeName: group
  maximumAuthenticationLifetime: 10
  usernameAttributeName: user
  emailAttributeName: email
  logoutUrl: http://logout
  usernameCaseConversion: lowercase
  binding: "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
  samlCustomAttributes:
  - name: xxx
    displayName: wierdxxx
    \$class: Attribute
""")
    def samlRealm = configHandler.setupSaml(config)
    assert samlRealm instanceof org.jenkinsci.plugins.saml.SamlSecurityRealm
    assert samlRealm.idpMetadataConfiguration.xml == '<xml></xml>'
    assert samlRealm.idpMetadataConfiguration.url == 'http://xxx.yyy'
    assert samlRealm.idpMetadataConfiguration.period == 10
    assert samlRealm.displayNameAttributeName == 'displayName'
    assert samlRealm.maximumAuthenticationLifetime == 10
    assert samlRealm.usernameAttributeName == 'user'
    assert samlRealm.emailAttributeName == 'email'
    assert samlRealm.logoutUrl == 'http://logout'
    assert samlRealm.usernameCaseConversion == 'lowercase'
    assert samlRealm.binding == 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST'
    assert samlRealm.samlCustomAttributes == [new org.jenkinsci.plugins.saml.conf.Attribute('xxx', 'wierdxxx')]
}

def testLdap(){
	def config = new Yaml().load("""
groupMembershipAttribute: memberOf
server: ldap.mydomain.com
rootDN: DC=mydomain,DC=com
userSearchBase: OU=users,DC=mydomain,DC=com
userSearchFilter: CN={1}
groupSearchBase: OU=groups,DC=mydomain,DC=com
groupSearchFilter: CN={1}
managerDN: CN=search,OU=users,DC=mydomain,DC=com
managerPassword: xxxxxx
inhibitInferRootDN: true
disableMailAddressResolver: true
connectTimeout: 6000
readTimeout: 100000
displayNameAttr: displayName
emailAttr: email
""")
    def ldapRealm = configHandler.setupLdap(config)
    assert ldapRealm instanceof hudson.security.LDAPSecurityRealm
    def ldapConfig = ldapRealm.configurations[0]
    assert ldapConfig.server == 'ldap.mydomain.com'
    assert ldapConfig.rootDN == 'DC=mydomain,DC=com'
    assert ldapConfig.userSearchBase == 'OU=users,DC=mydomain,DC=com'
    assert ldapConfig.userSearch == 'CN={1}'
    assert ldapConfig.groupSearchBase == 'OU=groups,DC=mydomain,DC=com'
    assert ldapConfig.groupSearchFilter == 'CN={1}'
    assert ldapConfig.managerDN == 'CN=search,OU=users,DC=mydomain,DC=com'
    assert ldapConfig.managerPassword == 'xxxxxx'
    assert ldapConfig.inhibitInferRootDN
    assert ldapRealm.disableMailAddressResolver
    assert ldapRealm.environmentProperties.find{it.name == 'com.sun.jndi.ldap.connect.timeout'}.value == '6000'
    assert ldapRealm.environmentProperties.find{it.name == 'com.sun.jndi.ldap.read.timeout'}.value == '100000'
    assert ldapConfig.displayNameAttributeName == 'displayName'
    assert ldapConfig.mailAddressAttributeName == 'email'
}


def testActiveDirectory(){
	def config = new Yaml().load("""
domains:
  - name: my-domain.com
    servers:
      - dc1.com
      - dc2.com
    site: site
    bindName: my-domain.com\\search
    bindPassword: password
    tlsConfiguration: TRUST_ALL_CERTIFICATES
groupLookupStrategy: RECURSIVE
removeIrrelevantGroups: true
customDomain: true
cache:
  size: 400
  ttl: 400
startTls: true
jenkinsInternalUser: admin
""")
    def realm = configHandler.setupActiveDirectory(config)
    assert realm instanceof hudson.plugins.active_directory.ActiveDirectorySecurityRealm
    def adDomain = realm.domains[0]
    assert adDomain.name == 'my-domain.com'
    assert adDomain.servers == 'dc1.com:3268,dc2.com:3268'
    assert adDomain.site == 'site'
    assert adDomain.bindName == 'my-domain.com\\search'
    assert adDomain.bindPassword.toString() == 'password'
    assert adDomain.tlsConfiguration == hudson.plugins.active_directory.TlsConfiguration.TRUST_ALL_CERTIFICATES
    assert realm.groupLookupStrategy == hudson.plugins.active_directory.GroupLookupStrategy.RECURSIVE
    assert realm.cache.size == 400
    assert realm.cache.ttl == 400
    assert realm.startTls
    assert realm.internalUsersDatabase.jenkinsInternalUser == 'admin'
}

def testAuthorizationStrategy(){
	def config = new Yaml().load("""
permissions:
  authenticated:
    - hudson.model.Hudson.Read
    - hudson.model.Item.Read
    - hudson.model.Item.Discover
    - hudson.model.Item.Cancel
  junior-developers:
    - hudson.model.Item.Read
"""
    )
    def strategy = configHandler.createAuthorizationStrategy(config, 'admin')
    assert strategy instanceof hudson.security.ProjectMatrixAuthorizationStrategy
    def grantedPermissions = strategy.grantedPermissions
    assert grantedPermissions[hudson.security.Permission.fromId('hudson.model.Hudson.Read')] == (['authenticated'] as Set)
    assert grantedPermissions[hudson.security.Permission.fromId('hudson.model.Item.Read')] == (['authenticated', 'junior-developers'] as Set)
    assert grantedPermissions[hudson.security.Permission.fromId('hudson.model.Item.Discover')] == (['authenticated'] as Set)
    assert grantedPermissions[hudson.security.Permission.fromId('hudson.model.Item.Cancel')] == (['authenticated'] as Set)
    assert grantedPermissions[hudson.security.Permission.fromId('hudson.model.Hudson.Administer')] == (['admin'] as Set)
 }

def testSecurityOptions(){
    def config = new Yaml().load("""
preventCSRF: false
enableScriptSecurityForDSL: true
enableAgentMasterAccessControl: false
disableRememberMe: true
sshdEnabled: true
jnlpProtocols:
  - JNLP
  - JNLP2
  - JNLP3
  - JNLP4
"""
    )
    configHandler.setupSecurityOptions(config)
    assert !jenkins.model.Jenkins.instance.crumbIssuer
    assert jenkins.model.GlobalConfiguration.all().get(javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration).useScriptSecurity
    assert jenkins.model.Jenkins.instance.disableRememberMe
    assert jenkins.model.Jenkins.instance.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule).masterKillSwitch
    assert jenkins.model.Jenkins.instance.agentProtocols == (['','2','4'].collect{"JNLP$it-connect".toString()} +['Ping']) as Set
    assert org.jenkinsci.main.modules.sshd.SSHD.get().port == 16022

    configHandler.setupSecurityOptions(null)
    assert jenkins.model.Jenkins.instance.crumbIssuer && jenkins.model.Jenkins.instance.crumbIssuer.excludeClientIPFromCrumb
    assert !jenkins.model.GlobalConfiguration.all().get(javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration).useScriptSecurity
    assert !jenkins.model.Jenkins.instance.disableRememberMe
    assert !jenkins.model.Jenkins.instance.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule).masterKillSwitch
    assert jenkins.model.Jenkins.instance.agentProtocols == (['4'].collect{"JNLP$it-connect".toString()} +['Ping']) as Set
    assert org.jenkinsci.main.modules.sshd.SSHD.get().port == -1

 }

 def testJenkinsDatabase(){
    def config = new Yaml().load("""
adminUser: admin
adminPassword: admin
"""
    )
    def realm = configHandler.setupJenkinsDatabase(config)
    assert (realm instanceof hudson.security.HudsonPrivateSecurityRealm)
    assert hudson.model.User.all.size() == 1
 }


def testPlainTextMarkupFormatter(){
    def config = new Yaml().load("""
markupFormatter: plainText
"""
    )
    configHandler.setupSecurityOptions(config)
    assert jenkins.model.Jenkins.instance.markupFormatter instanceof hudson.markup.EscapedMarkupFormatter
}

def testSafeHtmlMarkupFormatter(){
    def config = new Yaml().load("""
markupFormatter: safeHtml
"""
    )
    configHandler.setupSecurityOptions(config)
    assert jenkins.model.Jenkins.instance.markupFormatter instanceof hudson.markup.RawHtmlMarkupFormatter
}

def testRawHtmlMarkupFormatter(){
    def config = new Yaml().load("""
markupFormatter:
  rawHtmlMarkupFormatter:
"""
    )
    configHandler.setupSecurityOptions(config)
    assert jenkins.model.Jenkins.instance.markupFormatter instanceof hudson.markup.RawHtmlMarkupFormatter
    assert !jenkins.model.Jenkins.instance.markupFormatter.disableSyntaxHighlighting
}

def testRawHtmlMarkupFormatterWithDisableSyntaxHighlighting(){
    def config = new Yaml().load("""
markupFormatter:
  rawHtmlMarkupFormatter:
    disableSyntaxHighlighting: true
"""
    )
    configHandler.setupSecurityOptions(config)
    assert jenkins.model.Jenkins.instance.markupFormatter instanceof hudson.markup.RawHtmlMarkupFormatter
    assert jenkins.model.Jenkins.instance.markupFormatter.disableSyntaxHighlighting
}

testGoogleLogin()
testSaml()
testLdap()
testActiveDirectory()
testAuthorizationStrategy()
testSecurityOptions()
testJenkinsDatabase()
testPlainTextMarkupFormatter()
testSafeHtmlMarkupFormatter()
testRawHtmlMarkupFormatter()
testRawHtmlMarkupFormatterWithDisableSyntaxHighlighting()