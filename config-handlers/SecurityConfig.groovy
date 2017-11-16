import hudson.security.LDAPSecurityRealm
import hudson.security.ProjectMatrixAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.FromUserRecordLDAPGroupMembershipStrategy
import jenkins.model.Jenkins
import hudson.model.Hudson
import hudson.model.Item

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setupActiveDirectory(config){
    config.with{
        return new hudson.plugins.active_directory.ActiveDirectorySecurityRealm(
            domain, 
            domains?.collect{ currentDomain ->
                new hudson.plugins.active_directory.ActiveDirectoryDomain(
                    currentDomain.name, 
                    currentDomain.servers?.join(','), 
                    currentDomain.site, 
                    currentDomain.bindName, 
                    currentDomain.bindPassword
                )
            }, 
            site, 
            bindName,
            bindPassword, 
            server, 
            groupLookupStrategy ? hudson.plugins.active_directory.GroupLookupStrategy.valueOf(groupLookupStrategy) : null, 
            asBoolean(removeIrrelevantGroups), 
            asBoolean(customDomain, null), 
            cache ? new hudson.plugins.active_directory.CacheConfiguration(
                asInt(cache.size, 0),
                asInt(cache.ttl, 0)
            ) : null, 
            asBoolean(startTls, null), 
            tlsConfiguration ? hudson.plugins.active_directory.TlsConfiguration.valueOf(tlsConfiguration) : null, 
            jenkinsInternalUser ? new hudson.plugins.active_directory.ActiveDirectoryInternalUsersDatabase(jenkinsInternalUser) : null
        )
    }
}

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
                hudson.util.Secret.fromString(managerPassword),
                asBoolean(inhibitInferRootDN),
                asBoolean(disableMailAddressResolver),
                new LDAPSecurityRealm.CacheConfiguration(20, 300), [
                    new LDAPSecurityRealm.EnvironmentProperty('com.sun.jndi.ldap.connect.timeout', asInt(connectTimeout, 5000).toString()),
                    new LDAPSecurityRealm.EnvironmentProperty('com.sun.jndi.ldap.read.timeout', asInt(readTimeout, 60000).toString()),
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
        securityRealm = new HudsonPrivateSecurityRealm(false)
        securityRealm.createAccount(adminUser, adminPassword)
        return securityRealm
    }
}

def setup(config){
    config = config ?: [:]
    def adminUser = config.adminUser
    def instance = Jenkins.getInstance()

    def realm
    switch(config.realm){
        case 'ldap':
            realm = setupLdap(config)
            break
        case 'jenkins_database':
            realm = setupJenkinsDatabase(config)
            break
        case 'active_directory':
            realm = setupActiveDirectory(config)
            break
    }
    if(realm){
        instance.setSecurityRealm(realm)
        def strategy = new ProjectMatrixAuthorizationStrategy()
        strategy.add(Hudson.ADMINISTER, adminUser)
        config?.permissions?.each{ principal, permissions ->
            for(p in permissions){
                try{
                    def permission = hudson.security.Permission.fromId(p)
                    strategy.add(permission, principal)
                }catch(e){
                    println "Failed to set permission ${p} for principal ${principal}... ${e}"
                    e.printStackTrace()
                }
            }
        }

        instance.setAuthorizationStrategy(strategy)
        instance.save()
    }
}

return this