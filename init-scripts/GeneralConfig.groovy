import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import jenkins.CLI

def setup(config){
    println "General setup: config = ${config}"
    def instance = Jenkins.getInstance()
    config.with{
        instance.setNumExecutors(executers_count  ? executers_count as int : 0)
        CLI.get().setEnabled(cli_over_remoting ? cli_over_remoting.toBoolean() : false)

    }
}
return this