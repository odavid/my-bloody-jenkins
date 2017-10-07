import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def setup(config){
    config = config ?: [:]
    config.approvals?.each{ line ->
      ScriptApproval.get().approveSignature(line)    
    }
}
return this