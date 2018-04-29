import hudson.EnvVars
import javaposse.jobdsl.plugin.JenkinsDslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import javaposse.jobdsl.plugin.LookupStrategy

def setup(config){
    config = config ?: []
    def jenkinsJobManagement = new JenkinsJobManagement(System.out, new EnvVars(), null, null, LookupStrategy.JENKINS_ROOT)    
    config.each{jobDslScript -> 
        try{
            new JenkinsDslScriptLoader(jenkinsJobManagement).runScript(jobDslScript)
        }catch(e){
            println "Could not run jobDslScript: ${jobDslScript}, error: ${e}, skipping."
        }
    }
}

return this

