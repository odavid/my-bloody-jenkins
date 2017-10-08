import jenkins.model.Jenkins

def saveTool(tool, desc){
    def installations = desc.installations ? desc.installations as List : []
    def orig = desc.installations.find{it.name == tool.name}
    if(orig){
        installations.remove(orig)
    }
    installations << tool
    desc.setInstallations(installations.toArray(desc.installations))
}
def jdkConfig(config){
    def desc = Jenkins.instance.getDescriptorByType(hudson.model.JDK.DescriptorImpl)
    def tool = new hudson.model.JDK(config.name, config.home)
    saveTool(tool, desc)
}

def xvfbConfig(config){
    def desc = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.xvfb.Xvfb.XvfbBuildWrapperDescriptor)
    def tool = new org.jenkinsci.plugins.xvfb.XvfbInstallation(config.name, config.home, [])
    saveTool(tool, desc)
}

def mavenConfig(config){
    def desc = Jenkins.instance.getDescriptorByType(hudson.tasks.Maven.DescriptorImpl)
    def tool = new hudson.tasks.Maven.MavenInstallation(config.name, config.home, [])
    saveTool(tool, desc)
}

def antConfig(config){
    def desc = Jenkins.instance.getDescriptorByType(hudson.tasks.Ant.AntInstallation.DescriptorImpl)
    def tool = new hudson.tasks.Ant.AntInstallation(config.name, config.home, [])
    saveTool(tool, desc)
}

def gradleConfig(config){
    def desc = Jenkins.instance.getDescriptorByType(hudson.plugins.gradle.GradleInstallation.DescriptorImpl)
    def tool = new hudson.plugins.gradle.GradleInstallation(config.name, config.home, [])
    saveTool(tool, desc)
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

