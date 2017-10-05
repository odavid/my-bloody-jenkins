import hudson.security.LDAPSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.FromUserRecordLDAPGroupMembershipStrategy
import jenkins.model.Jenkins
import hudson.model.Hudson
import hudson.model.Item

def setupLdap(config){
    config.with{
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
}
def setupJenkinsDatabase(config){
    config.with{
        realm = new HudsonPrivateSecurityRealm(false)
        realm.createAccount(admin_user, admin_password)
        return realm
    }
}

def setupSecurityRealm(securityRealm, config){
    def realm
    switch(securityRealm){
        case 'ldap':
            realm = setup_ldap(config)
            break
        case 'jenkins_database':
            realm = setupJenkinsDatabase(config)
            break
    }
    if(realm){
        instance.setSecurityRealm(realm)
        instance.save()
        def strategy = instance.authorizationStrategy instanceof GlobalMatrixAuthorizationStrategy ? 
            instance.authorizationStrategy : new GlobalMatrixAuthorizationStrategy()
        config.with{
            strategy.add(Jenkins.READ, 'authenticated')
            strategy.add(Item.READ, 'authenticated')
            strategy.add(Item.DISCOVER, 'authenticated')
            strategy.add(Item.CANCEL, 'authenticated')
            strategy.add(Hudson.ADMINISTER, admin_user)
        }

        instance.setAuthorizationStrategy(strategy)
        instance.save()
    }
}

return this