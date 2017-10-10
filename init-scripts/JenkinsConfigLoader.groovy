import org.yaml.snakeyaml.Yaml
import jenkins.security.ApiTokenProperty
import hudson.model.User

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}

def handleConfig(handler, config){
    if(!config){
        println "--> skipping ${handler} configuration"
        return
    }
    println "--> Handling ${handler} configuration"
    try{
        evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy")).setup(config)
        println "--> Handling ${handler} configuration... done"
    }catch(e){
        println "--> Handling ${handler} configuration... error: ${e}"
        e.printStackTrace()
    }
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
jenkinsConfig.security?.adminUser = adminUser

handleConfig('General', jenkinsConfig.general)
handleConfig('Creds', jenkinsConfig.credentials)
handleConfig('Security', jenkinsConfig.security)
handleConfig('Clouds', jenkinsConfig.clouds)
handleConfig('Notifiers', jenkinsConfig.notifiers)
handleConfig('ScriptApproval', jenkinsConfig.script_approval)
handleConfig('Tools', jenkinsConfig.tools)
handleConfig('SonarQubeServers', jenkinsConfig.sonar_qube_servers)
handleConfig('Jira', jenkinsConfig.jira)
handleConfig('Checkmarx', jenkinsConfig.checkmarx)
handleConfig('PipelineLibraries', jenkinsConfig.pipeline_libraries)
handleConfig('SeedJobs', jenkinsConfig.seed_jobs)
