import org.yaml.snakeyaml.Yaml
import jenkins.security.ApiTokenProperty
import hudson.model.User

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}

def handleConfig(handler, config){
    println "Handling ${handler} configuration"
    evaluate(new File("/usr/share/jenkins/config-handlers/${handler}.groovy")).setup(config)
    println "Handled ${handler} configuration"
}

def getAdminUserName(){
    def env = System.getenv()
    return env['JENKINS_ENV_ADMIN_USER'] ?: 'admin'
}
def storeAdminApiToken(adminUser, filename){
    def adminUserApiToken = User.get(adminUser).getProperty(ApiTokenProperty).apiTokenInsecure
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
