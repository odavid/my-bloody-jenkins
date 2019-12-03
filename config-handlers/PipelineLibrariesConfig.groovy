import org.jenkinsci.plugins.structs.describable.DescribableModel
import jenkins.model.Jenkins

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def tryCreateDynamicSCMSource(libraryName, config){
    def type = config['$class']
    def klass
    if(type.contains('.')){
        try{
            klass = Class.forName(type)
        }catch(e){
            println "Could not find SCMSource class for symbol: $type, skipping pipeline lib: $libraryName"
            return null
        }
    }else{
        def matchedDescriptor = Jenkins.get().getDescriptorList(jenkins.scm.api.SCMSource)
            .find{ it.klass.toJavaClass().simpleName.toLowerCase().startsWith(type.toLowerCase()) }
        if(!matchedDescriptor){
            println "Could not find SCMSource class for symbol: $type, skipping pipeline lib: $libraryName"
            return null
        }
        klass = matchedDescriptor.klass.toJavaClass()
    }
    DescribableModel model
    try{
        model = DescribableModel.of(klass)
    }catch(e){
        println "Could not find DescribableModel for: $klass, skipping pipeline lib: $libraryName"
        return null
    }
    try{
        return model.instantiate(config)
    }catch(e){
        println "Could not instantiate DescribableModel for: $klass, skipping pipeline lib: $libraryName"
        e.printStackTrace(out)
        return null
    }
}



def libraryConfig(config){
    config.with{
        def scmConfig = retriever?.scm
        def scm
        if(!scmConfig){
            scm = new jenkins.plugins.git.GitSCMSource(
                "git-scm-${name}",
                source?.remote,
                source?.credentialsId,
                source?.includes ?: '*',  //includes
                source?.excludes ?: '',   //excludes
                asBoolean(source.ignoreOnPushNotifications) //ignoreOnPushNotifications
            )
        }else{
            scm = tryCreateDynamicSCMSource(name, scmConfig)
        }
        if(scm){
            //support of legacySCM
            def scmSource = scm instanceof hudson.scm.SCM ? new SCMRetriever(scm) : new SCMSourceRetriever(scm)
            def libraryConfiguration = new org.jenkinsci.plugins.workflow.libs.LibraryConfiguration(
                    name, scmSource
            )
            libraryConfiguration.defaultVersion = defaultVersion
            libraryConfiguration.implicit = asBoolean(implicit)
            libraryConfiguration.allowVersionOverride = asBoolean(allowVersionOverride, true)
            libraryConfiguration.includeInChangesets = asBoolean(includeInChangesets, true)
            return libraryConfiguration
        }
        return null
    }
}

def setup(config){
    config = config ?: [:]
    org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries = config.collect{ k,v ->
        libraryConfig([name: k] + v)
    }.grep()
}
return this