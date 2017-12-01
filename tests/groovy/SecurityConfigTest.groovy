import org.yaml.snakeyaml.Yaml

handler = 'Security'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

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
groupLookupStrategy: RECURSIVE
removeIrrelevantGroups: true
customDomain: true
cache:
  size: 400
  ttl: 400
startTls: true
tlsConfiguration: TRUST_ALL_CERTIFICATES
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
    assert realm.groupLookupStrategy == hudson.plugins.active_directory.GroupLookupStrategy.RECURSIVE
    assert realm.cache.size == 400
    assert realm.cache.ttl == 400
    assert realm.startTls
    assert realm.tlsConfiguration == hudson.plugins.active_directory.TlsConfiguration.TRUST_ALL_CERTIFICATES
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

testLdap()
testActiveDirectory()
testAuthorizationStrategy()