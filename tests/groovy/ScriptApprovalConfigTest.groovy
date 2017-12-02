import org.yaml.snakeyaml.Yaml

handler = 'ScriptApproval'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testApproval(){
 	def config = new Yaml().load("""
approvals:
  - field hudson.model.Queue\$Item task
  - method groovy.lang.Binding getVariable java.lang.String
  - method groovy.lang.Binding getVariables
  - method groovy.lang.Binding hasVariable java.lang.String
""")
    configHandler.setup(config)
    def approvedSignatures = org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval.get().approvedSignatures
    assert approvedSignatures.length == 4
    assert 'field hudson.model.Queue$Item task' in approvedSignatures
    assert 'method groovy.lang.Binding getVariable java.lang.String' in approvedSignatures
    assert 'method groovy.lang.Binding getVariables' in approvedSignatures
    assert 'method groovy.lang.Binding hasVariable java.lang.String' in approvedSignatures
}

testApproval()