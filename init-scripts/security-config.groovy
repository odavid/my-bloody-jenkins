import hudson.security.LDAPSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.FromUserRecordLDAPGroupMembershipStrategy
import jenkins.model.Jenkins
import hudson.model.Hudson
import hudson.model.Item

def shared = evaluate(new File("/var/jenkins_home/init.groovy.d/SharedMethods.groovy"))
Properties envProperties = shared.loadProperties()

def setup_ldap( server, rootDN, 
                userSearchBase, userSearchFilter,
                groupSearchBase, groupSearchFilter,
                groupMembershipAttribute, groupMembershipFilter,
                displayNameAttr, emailAttr,
                managerDN, managerPassword){
    if (!groupMembershipAttribute && !groupMembershipFilter){
        throw new IllegalArgumentException("One of: groupMembershipFilter, groupMembershipAttribute must be provided")
    }

    def groupMembershipStrategy = groupMembershipAttribute ?
        new FromUserRecordLDAPGroupMembershipStrategy(groupMembershipAttribute) :
        new FromGroupSearchLDAPGroupMembershipStrategy(groupMembershipFilter)
    return new LDAPSecurityRealm(
            server,
            rootDN,
            userSearchBase,
            userSearchFilter,
            groupSearchBase,
            groupSearchFilter,
            groupMembershipStrategy,
            managerDN,
            Secret.fromString(managerPassword),
            false,
            false,
            new LDAPSecurityRealm.CacheConfiguration(20, 300), [
                new LDAPSecurityRealm.EnvironmentProperty('com.sun.jndi.ldap.connect.timeout', '5000'),
                new LDAPSecurityRealm.EnvironmentProperty('com.sun.jndi.ldap.read.timeout', '60000'),
            ] as LDAPSecurityRealm.EnvironmentProperty[],
            displayNameAttr, 
            emailAttr,
            /*IdStrategy userIdStrategy*/null,
            /*IdStrategy groupIdStrategy*/null
    )
}

def instance = Jenkins.getInstance()

def security_realm = envProperties.get('SECURITY_REALM')
def admin_user = envProperties.get('ADMIN_USER')
def admin_password = envProperties.get('ADMIN_PASSWORD')
def server = envProperties.get('LDAP_SERVER')
def rootDN = envProperties.get('LDAP_ROOT_DN')
def userSearchBase = envProperties.get('LDAP_USER_SEARCH_BASE')
def userSearchFilter = envProperties.get('LDAP_USER_SERACH_FILTER')
def groupSearchBase = envProperties.get('LDAP_GROUP_SEARCH_BASE')
def groupSearchFilter = envProperties.get('LDAP_GROUP_SEARCH_FILTER')
def groupMembershipAttribute = envProperties.get('LDAP_GROUP_MEMBERSHIP_ATTRIBUTE')
def groupMembershipFilter = envProperties.get('LDAP_GROUP_MEMBERSHIP_FILTER')
def displayNameAttr = envProperties.get('LDAP_DISPLAY_NAME_ATTRIBUTE')
def emailAttr = envProperties.get('LDAP_EMAIL_ATTRIBUTE')
def managerDN = envProperties.get('LDAP_MANAGER_DN')
def managerPassword = envProperties.get('LDAP_MANAGER_PASSWORD')

def realm
switch(security_realm){
    case 'ldap':
        realm = setup_ldap(
            server, rootDN, 
            userSearchBase, userSearchFilter,
            groupSearchBase, groupSearchFilter,
            groupMembershipAttribute, groupMembershipFilter,
            displayNameAttr, emailAttr,
            managerDN, managerPassword
        )
        break
    case 'jenkins_database':
        realm = new HudsonPrivateSecurityRealm(false)
        realm.createAccount(admin_user, admin_password)
        break
}

if(realm){
    instance.setSecurityRealm(realm)
    instance.save()
    
    def strategy = instance.authorizationStrategy instanceof GlobalMatrixAuthorizationStrategy ? 
        instance.authorizationStrategy : new GlobalMatrixAuthorizationStrategy()
    
    strategy.add(Jenkins.READ, 'authenticated')
    strategy.add(Item.READ, 'authenticated')
    strategy.add(Item.DISCOVER, 'authenticated')
    strategy.add(Item.CANCEL, 'authenticated')
    strategy.add(Hudson.ADMINISTER, admin_user)

    instance.setAuthorizationStrategy(strategy)
    instance.save()
}


