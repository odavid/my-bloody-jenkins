import org.yaml.snakeyaml.Yaml

handler = 'EnvironmentVars'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testEnvVars(){
 	def config = new Yaml().load("""
SIMPLE_ENV: SIMPLE_VALUE
""")

    configHandler.setup(config)
    def nodeProperties = jenkins.model.Jenkins.instance.globalNodeProperties.get(hudson.slaves.EnvironmentVariablesNodeProperty)
    assert nodeProperties.envVars.expand('ABC${SIMPLE_ENV}') == 'ABCSIMPLE_VALUE'

}

testEnvVars()