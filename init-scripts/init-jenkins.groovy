import org.yaml.snakeyaml.Yaml

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}

def generalConfig = evaluate(new File("/usr/share/jenkins/config-handlers/GeneralConfig.groovy"))
def credsConfig = evaluate(new File("/usr/share/jenkins/config-handlers/CredsConfig.groovy"))
def securityConfig = evaluate(new File("/usr/share/jenkins/config-handlers/SecurityConfig.groovy"))
def cloudsConfig = evaluate(new File("/usr/share/jenkins/config-handlers/CloudsConfig.groovy"))

def jenkinsConfig = loadYamlConfig('/etc/jenkins-config.yml')

generalConfig.setup(jenkinsConfig.general)
credsConfig.setup(jenkinsConfig.credentials)
securityConfig.setup(jenkinsConfig.security)
cloudsConfig.setup(jenkinsConfig.clouds)
