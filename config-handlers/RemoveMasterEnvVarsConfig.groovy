def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: []
    hudson.EnvVars.masterEnvVars
        .findAll{k,v -> config.findAll{k ==~ it}}
        .keySet()
        .each{hudson.EnvVars.masterEnvVars.remove(it)}

}
return this
