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
        desc.artifactoryServers = artifactoryServers?.collect{artifactoryServer ->
            DescribableModel.of(org.jfrog.hudson.ArtifactoryServer).instantiate(artifactoryServer)
        }
    }
    desc.save()
}
return this
