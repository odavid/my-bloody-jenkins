
def jdkConfig(config){

}

def xvfbConfig(config){

}

def mavenConfig(config){

}

def antConfig(config){

}

def gradleConfig(config){

}

def setup(config){
    config = config ?: [:]
    config.collect{k,v -> 
        def toolConfig = [name: k] << v
        switch(v.type){
            case 'jdk':
                return jdkConfig(toolConfig)
            case 'xvfb':
                return xvfbConfig(toolConfig)
            case 'maven':
                return mavenConfig(toolConfig)
            case 'ant':
                return antConfig(toolConfig)
            case 'gradle':
                return gradleConfig(toolConfig)
            default:
                return null
        }
    }.grep().each{
        it.save()
    }
}

return this

