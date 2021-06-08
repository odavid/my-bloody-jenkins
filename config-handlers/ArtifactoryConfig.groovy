import org.jenkinsci.plugins.structs.describable.DescribableModel

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def desc = jenkins.model.Jenkins.instance.getDescriptor(org.jfrog.hudson.ArtifactoryBuilder)
    config.with{
        desc.useCredentialsPlugin = asBoolean(useCredentialsPlugin)
        // artifactoryServers are for backward compatibility
        servers = jfrogInstances ?: artifactoryServers
        desc.artifactoryServers = servers?.collect{jfrogInstance ->
            if(jfrogInstance.serverId && !jfrogInstance.instanceId){
                jfrogInstance.instanceId = jfrogInstance.serverId
                jfrogInstance.remove('serverId')
            } else if(jfrogInstance.serverId){
                jfrogInstance.remove('serverId')
            }
            DescribableModel.of(org.jfrog.hudson.JFrogPlatformInstance).instantiate(jfrogInstance)
        }
    }
    desc.save()
}
return this
