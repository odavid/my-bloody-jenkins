import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testToolAutoInstaller(){
	def config = new Yaml().load("""
installations:
  sonar-latest:
   type: sonarQubeRunner
   installers:
     - id: '3.0.3.778'
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.sonar.SonarRunnerInstallation)
	def installation = desc.installations.find{it.name == 'sonar-latest'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.plugins.sonar.SonarRunnerInstaller
	assert installation.properties[0].installers[0].id == '3.0.3.778'
}

def testToolManualInstaller(){
	def config = new Yaml().load("""
installations:
  sonar-latest:
   type: sonarQubeRunner
   home: /user/share/sonar-latest
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.sonar.SonarRunnerInstallation)
	def installation = desc.installations.find{it.name == 'sonar-latest'}
	assert installation.home == '/user/share/sonar-latest'
}

testToolAutoInstaller()
testToolManualInstaller()
