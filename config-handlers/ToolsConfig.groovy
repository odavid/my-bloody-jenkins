import jenkins.model.Jenkins

def createInstallation(config){
    def installation = null
    def installers = config.installers?.collect{createInstaller(config.type, it)}
    def toolsProperties = installers ? [new hudson.tools.InstallSourceProperty(installers)] : []
    def desc = null
    config.home = config.home?:''
    switch(config?.type){
        case 'jdk':
            desc = Jenkins.instance.getDescriptorByType(hudson.model.JDK.DescriptorImpl)
            installation = new hudson.model.JDK(config.name, config.home, toolsProperties)
            break
        case 'xvfb':
            desc = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.xvfb.Xvfb.XvfbBuildWrapperDescriptor)
            installation = new org.jenkinsci.plugins.xvfb.XvfbInstallation(config.name, config.home, toolsProperties)
            break
        case 'maven':
            desc = Jenkins.instance.getDescriptorByType(hudson.tasks.Maven.DescriptorImpl)
            installation = new hudson.tasks.Maven.MavenInstallation(config.name, config.home, toolsProperties)
            break
        case 'ant':
            desc = Jenkins.instance.getDescriptorByType(hudson.tasks.Ant.AntInstallation.DescriptorImpl)
            installation = new hudson.tasks.Ant.AntInstallation(config.name, config.home, toolsProperties)
            break
        case 'gradle':
            desc = Jenkins.instance.getDescriptorByType(hudson.plugins.gradle.GradleInstallation.DescriptorImpl)
            installation = new hudson.plugins.gradle.GradleInstallation(config.name, config.home, toolsProperties)
            break
        case 'sonarQubeRunner':
            desc = Jenkins.instance.getDescriptor(hudson.plugins.sonar.SonarRunnerInstallation)
            installation = new hudson.plugins.sonar.SonarRunnerInstallation(config.name, config.home, toolsProperties)
            break
        case 'golang':
            desc = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.golang.GolangInstallation.DescriptorImpl)
            installation = new org.jenkinsci.plugins.golang.GolangInstallation(config.name, config.home, toolsProperties)
            break
        default:
            return null
    }
    if(desc && installation){
        def installations = desc.installations ? desc.installations as List : []
        def orig = desc.installations.find{it.name == installation.name}
        if(orig){
            installations.remove(orig)
        }
        installations << installation
        desc.setInstallations(installations.toArray(desc.installations))
    }    
}

def createInstaller(toolType, installer){
    if('id' in installer){
        def id = installer.id
        switch(toolType){
            case 'jdk':
                return new hudson.tools.JDKInstaller(id, true)
            case 'maven':
                return new hudson.tasks.Maven.MavenInstaller(id)
            case 'ant':
                return new hudson.tasks.Ant.AntInstaller(id)
            case 'gradle':
                return new hudson.plugins.gradle.GradleInstaller(id)
            case 'sonarQubeRunner':
                return new hudson.plugins.sonar.SonarRunnerInstaller(id)
            case 'golang':
                return new org.jenkinsci.plugins.golang.GolangInstaller(id)
        }

    } else if('type' in installer){
        switch(installer.type){
            case 'command':
                return new hudson.tools.CommandInstaller(
                    installer.label,
                    installer.command,
                    installer.toolHome
                )
            case 'zip':
                return new hudson.tools.ZipExtractionInstaller(
                    installer.label,
                    installer.url,
                    installer.subdir
                )
        }
    }
    return null
}

def setup(config){
    config = config ?: [:]
    if(config.oracle_jdk_download?.username){
        def jdkInstaller = jenkins.model.Jenkins.instance.getDescriptor(hudson.tools.JDKInstaller)
        jdkInstaller.doPostCredential(
            config.oracle_jdk_download.username, 
            config.oracle_jdk_download.password
        )
    }

    config.installations?.each{k,v -> 
        def toolConfig = [name: k] << v
        createInstallation(toolConfig)
    }
}

return this

