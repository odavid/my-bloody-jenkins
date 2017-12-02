def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def libraryConfig(config){
    config.with{
        def libraryConfiguration = new org.jenkinsci.plugins.workflow.libs.LibraryConfiguration(
            name, 
            new org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever(
                new jenkins.plugins.git.GitSCMSource(
                    "git-scm-${name}",
                    source?.remote,
                    source?.credentialsId,
                    source?.includes ?: '*',  //includes
                    source?.excludes ?: '',   //excludes
                    asBoolean(source.ignoreOnPushNotifications) //ignoreOnPushNotifications
                )
            )
        )
        libraryConfiguration.defaultVersion = defaultVersion
        libraryConfiguration.implicit = asBoolean(implicit)
        libraryConfiguration.allowVersionOverride = asBoolean(allowVersionOverride, true)
        libraryConfiguration.includeInChangesets = asBoolean(includeInChangesets, true)
        return libraryConfiguration
    }
}

def setup(config){
    config = config ?: [:]
    org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries = config.collect{ k,v -> 
        libraryConfig([name: k] + v)
    }
}
return this