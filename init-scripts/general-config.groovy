import jenkins.model.Jenkins
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def instance = Jenkins.getInstance()
def jenkins_home = instance.getRootDir().getPath()

Properties envProperties = new Properties()
File propertiesFile = new File('/tmp/jenkins-env.properties')
propertiesFile.withInputStream {
    envProperties.load(it)
}

def executers_count = envProperties.get('EXECUTERS_COUNT') as int
instance.setNumExecutors(executers_count)

//Letting Jenkinsfile approve these methods
new File("${jenkins_home}/sandbox-signatures.txt").eachLine { line ->
    if(line){
        ScriptApproval.get().approveSignature(line)
    }
}