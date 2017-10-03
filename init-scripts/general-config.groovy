import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import jenkins.CLI

def shared = evaluate(new File("/var/jenkins_home/init.groovy.d/SharedMethods.groovy"))
Properties envProperties = shared.loadProperties()
def instance = Jenkins.getInstance()

def executers_count = envProperties.get('EXECUTERS_COUNT') as int
def cli_over_remoting = envProperties.get('CLI_OVER_REMOTING')

instance.setNumExecutors(executers_count)

CLI.get().setEnabled(cli_over_remoting.toBoolean())

//Letting Jenkinsfile approve these methods
new File("${instance.rootDir.path}/sandbox-signatures.txt").eachLine { line ->
    if(line){
        ScriptApproval.get().approveSignature(line)
    }
}