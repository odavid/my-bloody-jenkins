import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config){
    def env = System.getenv()
    def instance = Jenkins.getInstance()
    def slaveAgentPorts = env['JENKINS_SLAVE_AGENT_PORT']
    def executersCount = env['JENKINS_ENV_EXECUTERS']
    def changeWorkspaceDir = env['JENKINS_ENV_CHANGE_WORKSPACE_DIR']

    def jenkinsUrl = env['JENKINS_ENV_JENKINS_URL']
    def adminAddress = env['JENKINS_ENV_ADMIN_ADDRESS']

    if(slaveAgentPorts){
        Jenkins.instance.setSlaveAgentPort(asInt(slaveAgentPorts, 50000))
        Jenkins.instance.save()
    }

    if(jenkinsUrl || adminAddress){
        def jenkinsLocationConfig = jenkins.model.JenkinsLocationConfiguration.get()
        if(jenkinsUrl){
            jenkinsLocationConfig.url  = jenkinsUrl
        }
        if(adminAddress){
            jenkinsLocationConfig.adminAddress = adminAddress
        }
        jenkinsLocationConfig.save()
    }

    instance.setNumExecutors(executersCount  ? executersCount.toInteger() : 0)
    // This is the only way to change the workspaceDir field at the moment... ):
    // We do that if the JENKINS_HOME is mapped to NFS volume (e.g. deployment on ECS or Kubernetes)
    if(changeWorkspaceDir){
        def f = Jenkins.getDeclaredField('workspaceDir')
        f.setAccessible(true)
        f.set(Jenkins.instance, '/jenkins-workspace-home/workspace/${ITEM_FULLNAME}')
        Jenkins.instance.save()
    }


    Thread.start{
        sleep 1000
        println 'updating Downloadables'
        hudson.model.DownloadService.Downloadable.all().each{ d -> d.updateNow() }
        println 'updating Downloadables. done...'
    }

    // activate quiet start if user specified quiet start using JENKINS_ENV_QUIET_STARTUP_PERIOD
    File quietStartup = new File(env['QUIET_STARTUP_FILE_LOCATION'])
    if(!quietStartup.exists() && env['JENKINS_ENV_QUIET_STARTUP_PERIOD']){
        final def timeToWaitInSec = asInt(env['JENKINS_ENV_QUIET_STARTUP_PERIOD'])
        quietStartup.text = env['JENKINS_ENV_QUIET_STARTUP_PERIOD']
        println '--> Entering quiet mode'
        jenkins.model.Jenkins.instance.doQuietDown()
        Thread.start {
            Thread.sleep(timeToWaitInSec * 1000)
            hudson.security.ACL.impersonate(hudson.security.ACL.SYSTEM)
            jenkins.model.Jenkins.instance.doCancelQuietDown()
            println '--> Entering quiet mode. Done...'
        }
    }
}
return this