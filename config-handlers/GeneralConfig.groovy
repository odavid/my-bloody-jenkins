import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import jenkins.CLI

def setup(config){
    config = config ?: [:]
    def instance = Jenkins.getInstance()
    config.with{
        instance.setNumExecutors(executersCount  ? executersCount.toInteger() : 0)
        CLI.get().setEnabled(cliOverRemoting ? cliOverRemoting.toBoolean() : false)
    }
    // This is the only way to change the workspaceDir field at the moment... ):
    // We do that if the JENKINS_HOME is mapped to NFS volume (e.g. deployment on ECS or Kubernetes)
    if(config.changeWorkspaceDir){
        def f = Jenkins.getDeclaredField('workspaceDir')
        f.setAccessible(true)
        f.set(Jenkins.instance, '/jenkins-workspace-home/workspace/${ITEM_FULLNAME}')
        Jenkins.instance.save()
    }
}
return this