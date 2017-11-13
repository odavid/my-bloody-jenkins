def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def desc = jenkins.model.Jenkins.instance.getDescriptor(com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig)
    def connections = config.connections?.collect{ connectionConfig ->
        connectionConfig.with{
            return new com.dabsquared.gitlabjenkins.connection.GitLabConnection(
                name,
                url,
                apiTokenId,
                clientBuilderId ?: 'autodetect', //autodetect, v3, v4 
                asBoolean(ignoreCertificateErrors),
                asInt(connectionTimeout, 10),
                asInt(readTimeout, 10)
            )
        }
    }
    desc.connections = connections
    desc.useAuthenticatedEndpoint = useAuthenticatedEndpoint != null ? useAuthenticatedEndpoint : true
    return desc
}
return this
