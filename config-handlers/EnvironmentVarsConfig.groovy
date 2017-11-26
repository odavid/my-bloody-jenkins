def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def nodeProperties = jenkins.model.Jenkins.instance.globalNodeProperties.get(hudson.slaves.EnvironmentVariablesNodeProperty)
    if(nodeProperties){
        jenkins.model.Jenkins.instance.globalNodeProperties.remove(nodeProperties)
    }
    def entries = config.collect{k, v ->
        new hudson.slaves.EnvironmentVariablesNodeProperty.Entry(k,v)
    }
    jenkins.model.Jenkins.instance.globalNodeProperties.add(new hudson.slaves.EnvironmentVariablesNodeProperty(entries))
    jenkins.model.Jenkins.instance.save()
}
return this
