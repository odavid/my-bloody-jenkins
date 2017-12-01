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

testLdap()