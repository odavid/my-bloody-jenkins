import org.jenkinsci.plugins.structs.describable.DescribableModel
import hudson.security.LDAPSecurityRealm
import hudson.security.ProjectMatrixAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.FromUserRecordLDAPGroupMembershipStrategy
import org.jenkinsci.plugins.oic.OicSecurityRealm
import hudson.security.AuthorizationStrategy
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
                    currentDomain.bindPassword,
                    currentDomain.tlsConfiguration ? hudson.plugins.active_directory.TlsConfiguration.valueOf(currentDomain.tlsConfiguration) : null
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
    def currnetRealm = jenkins.model.Jenkins.instance.securityRealm
    def securityRealm = (currnetRealm instanceof HudsonPrivateSecurityRealm) ? currnetRealm : new HudsonPrivateSecurityRealm(false)
    config.with{
        securityRealm.createAccount(adminUser, adminPassword)
        users?.each{ user ->
            securityRealm.createAccount(user.id, user.password)
        }
    }
    return securityRealm
}

def setupSaml(config){
    def realmConfig = config.realmConfig
    return realmConfig ? DescribableModel.of(org.jenkinsci.plugins.saml.SamlSecurityRealm).instantiate(realmConfig) : null
}

def setupGoogleOAuth2(config){
    def realmConfig = config.realmConfig
    return realmConfig ? DescribableModel.of(org.jenkinsci.plugins.googlelogin.GoogleOAuth2SecurityRealm).instantiate(realmConfig) : null
}


def createAuthorizationStrategy(config, adminUser){
    def strategy = new hudson.security.AuthorizationStrategy.Unsecured()
    if (!asBoolean(config['unsecureStrategy'], false)){
        strategy = new ProjectMatrixAuthorizationStrategy()
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
    }
    return strategy
}

def setupSecurityOptions(config){
    config = config ?: [:]
    // https://wiki.jenkins.io/display/JENKINS/CSRF+Protection
    config.preventCSRF = asBoolean(config.preventCSRF, true)
    config.enableScriptSecurityForDSL = asBoolean(config.enableScriptSecurityForDSL)
    config.disableRememberMe = asBoolean(config.disableRememberMe)
    config.sshdEnabled = asBoolean(config.sshdEnabled)
    config.markupFormatter = config.markupFormatter != null ? config.markupFormatter : 'plainText'

    config.with{
        if(preventCSRF){
            jenkins.model.Jenkins.instance.crumbIssuer = new hudson.security.csrf.DefaultCrumbIssuer(true)
        }else{
            jenkins.model.Jenkins.instance.setCrumbIssuer(null)
        }
        jenkins.model.GlobalConfiguration.all()
            .get(javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration)
            .useScriptSecurity = enableScriptSecurityForDSL
        jenkins.model.Jenkins.instance.disableRememberMe = disableRememberMe


        if(sshdEnabled){
            org.jenkinsci.main.modules.sshd.SSHD.get().port = 16022
        }else{
            org.jenkinsci.main.modules.sshd.SSHD.get().port = -1
        }

        def mupFormatter
        if((markupFormatter instanceof Map && markupFormatter.size() == 1)){
            def key = markupFormatter.keySet()[0]
            if(key.toLowerCase() == 'RawHtmlMarkupFormatter'.toLowerCase()){
                def disableSyntaxHighlighting = asBoolean(markupFormatter[(key)]?.disableSyntaxHighlighting, false)
                mupFormatter = new hudson.markup.RawHtmlMarkupFormatter(disableSyntaxHighlighting)
            }
        }else if(markupFormatter == 'plainText'){
            mupFormatter = new hudson.markup.EscapedMarkupFormatter()
        }else if(markupFormatter == 'safeHtml'){
            mupFormatter = new hudson.markup.RawHtmlMarkupFormatter(false)
        }
        if(mupFormatter){
            jenkins.model.Jenkins.instance.markupFormatter = mupFormatter
        }

        jenkins.model.Jenkins.instance.save()
    }
}

def setupOpenIDConnect(config){
    def realmConfig = config.realmConfig
    return realmConfig ? DescribableModel.of(org.jenkinsci.plugins.oic.OicSecurityRealm).instantiate(realmConfig) : null
}

def setupGithubOAuth2(config){
    def realmConfig = config.realmConfig
    return realmConfig ? DescribableModel.of(org.jenkinsci.plugins.GithubSecurityRealm).instantiate(realmConfig) : null
}

def setup(config){
    config = config ?: [:]
    setupSecurityOptions(config.securityOptions)

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
        case 'saml':
            realm = setupSaml(config)
            break
        case 'google':
            realm = setupGoogleOAuth2(config)
            break
        case 'oic':
            realm = setupOpenIDConnect(config)
            break
        case 'github':
            realm = setupGithubOAuth2(config)
            break
    }
    if(realm){
        instance.setSecurityRealm(realm)
        def strategy = createAuthorizationStrategy(config, adminUser)
        if (strategy instanceof hudson.security.ProjectMatrixAuthorizationStrategy){
            instance.setAuthorizationStrategy(strategy)
        }
        instance.save()
    }
}

return this