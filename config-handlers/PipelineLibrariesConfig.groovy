import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.libs.*
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def libraryConfig(config){
    config.with{
        def libraryConfiguration = new LibraryConfiguration(
            name, 
            new SCMSourceRetriever(
                new GitSCMSource(
                    "git-scm-${name}",
                    source?.remote,
                    source?.credentialsId,
                    '*',  //includes
                    '',   //excludes
                    false //ignoreOnPushNotifications
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
    def libs = Jenkins.instance.getDescriptor('org.jenkinsci.plugins.workflow.libs.GlobalLibraries')
    config = config ?: [:]
    libs.get().setLibraries(config.collect{ k,v -> 
        libraryConfig([name: k] + v)
    })
}
return this