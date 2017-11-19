def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    if(config.proxyHost){
        config.with{
            def pc = new hudson.ProxyConfiguration(proxyHost, port, userName, password, noProxyHost)
            jenkins.model.Jenkins.instance.proxy = pc
            pc.save()
        }
    }
}
return this
