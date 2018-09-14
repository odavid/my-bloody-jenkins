import org.yaml.snakeyaml.Yaml
import jenkins.security.ApiTokenProperty
import hudson.model.User

def replaceEnvironmentVariables(element){
    if(element instanceof Map){
        def newMap = [:]
        element.each{k, v -> newMap[(k)] = replaceEnvironmentVariables(v)}
        return newMap
    }
    if(element instanceof List){
        return element.collect{replaceEnvironmentVariables(it)}
    }
    if(element instanceof String){
        def finder = element =~ /\$\{(.*?)\}/
        if(finder.count){
            def keys = (0..(finder.count-1)).collect{finder[it][1]}
            def contextMap = [:]
            keys.each{
                if(!System.getenv(it)){
                    println "Cannot find env var: ${it}, setting empty value"
                    contextMap[(it)] = ''
                }else{
                    contextMap[(it)] = System.getenv(it)
                }
            }
            return new groovy.text.SimpleTemplateEngine().createTemplate(element).make(contextMap).toString()
        }
    }
    return element
}

def loadYamlConfig(filename){
    def conf = new File(filename).withReader{
        new Yaml().load(it)
    }
    conf = replaceEnvironmentVariables(conf)
    return conf
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

def handleCustomConfig(config){
    if(!config){
        return
    }
    File f = new File("/usr/share/jenkins/config-handlers/CustomConfig.groovy")
    if(f.exists()){
        handleConfig("Custom", config)
    }
}

def getAdminUserName(){
    return System.getenv()['JENKINS_ENV_ADMIN_USER']
}

def storeAdminApiToken(adminUser, filename){
    final String JENKINS_TOKEN_NAME = 'JENKINS_CLI_INTERNAL'

    def user = User.get(adminUser, true);
    def token = user.getProperty(ApiTokenProperty)
    def file = new File(filename)
    if(token.hasLegacyToken() || !token.tokenList || !token.tokenList.find{ it.name == JENKINS_TOKEN_NAME } || !file.exists()){
        println "generating a new token: $JENKINS_TOKEN_NAME"
        token.deleteApiToken()
        def generatedToken = token.tokenStore.generateNewToken(JENKINS_TOKEN_NAME)
        user.save()
        file.withWriter{out -> out.println "${adminUser}:${generatedToken.plainValue}"}
    }
}

def adminUser = getAdminUserName()
if(!adminUser){
    println "JENKINS_ENV_ADMIN_USER was not set. This is mandatory variable"
}else{
    storeAdminApiToken(adminUser, System.getenv()['TOKEN_FILE_LOCATION'])
}

def configFileName = System.getenv()['CONFIG_FILE_LOCATION']

if(!new File(configFileName).exists()) {
    println "${configFileName} does not exist. Set variable JENKINS_ENV_CONFIG_YAML! Skipping configuration..."
} else {
    def jenkinsConfig = loadYamlConfig(configFileName)
    if(!jenkinsConfig){
        println "jenkinsConfig is empty, skipping"
        return
    }
    // TODO: admin user should be global. Make it more generic....
    jenkinsConfig.security?.adminUser = adminUser

    // TODO: General config is using only environment variables
    // Find a more elegant way to handle it
    handleConfig('Proxy', jenkinsConfig.proxy)
    handleConfig('General', [general: true])
    handleConfig('RemoveMasterEnvVars', jenkinsConfig.remove_master_envvars)
    handleConfig('EnvironmentVars', jenkinsConfig.environment)
    handleConfig('Creds', jenkinsConfig.credentials)
    handleConfig('Security', jenkinsConfig.security)
    handleConfig('Clouds', jenkinsConfig.clouds)
    handleConfig('Notifiers', jenkinsConfig.notifiers)
    handleConfig('ScriptApproval', jenkinsConfig.script_approval)
    handleConfig('Tools', jenkinsConfig.tools)
    handleConfig('SonarQubeServers', jenkinsConfig.sonar_qube_servers)
    handleConfig('Jira', jenkinsConfig.jira)
    handleConfig('JiraSteps', jenkinsConfig.jiraSteps)
    handleConfig('Checkmarx', jenkinsConfig.checkmarx)
    handleConfig('Gitlab', jenkinsConfig.gitlab)
    handleConfig('PipelineLibraries', jenkinsConfig.pipeline_libraries)
    handleConfig('SeedJobs', jenkinsConfig.seed_jobs)
    handleConfig('JobDSLScripts', jenkinsConfig.job_dsl_scripts)

    handleCustomConfig(jenkinsConfig.customConfig)
}