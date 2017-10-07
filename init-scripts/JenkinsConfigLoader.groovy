import org.yaml.snakeyaml.Yaml
import jenkins.security.ApiTokenProperty
import hudson.model.User

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}

def handleConfig(handler, config){
    println "--> Handling ${handler} configuration"
    evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy")).setup(config)
    println "--> Handling ${handler} configuration... done"
}

def getAdminUserName(){
    return System.getenv()['JENKINS_ENV_ADMIN_USER']
}

def storeAdminApiToken(adminUser, filename){
    def adminUserApiToken = User.get(adminUser, true).getProperty(ApiTokenProperty).apiTokenInsecure
    new File(filename).withWriter{out -> out.println "${adminUser}:${adminUserApiToken}"}
}

def adminUser = getAdminUserName()
storeAdminApiToken(adminUser, '/tmp/.api-token')

def jenkinsConfig = loadYamlConfig('/etc/jenkins-config.yml')
// TODO: admin user should be global. Make it more generic....
jenkinsConfig.security?.admin_user = adminUser

handleConfig('General', jenkinsConfig.general)
handleConfig('Creds', jenkinsConfig.credentials)
handleConfig('Security', jenkinsConfig.security)
handleConfig('Clouds', jenkinsConfig.clouds)
handleConfig('Notifiers', jenkinsConfig.notifiers)
