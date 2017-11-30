import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testToolAutoInstaller(){
	def config = new Yaml().load("""
installations:
  GRADLE-4.3.1:
   type: gradle
   installers:
     - id: '4.3.1'
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.plugins.gradle.GradleInstallation.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'GRADLE-4.3.1'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.plugins.gradle.GradleInstaller
	assert installation.properties[0].installers[0].id == '4.3.1'
}

def testToolManualInstaller(){
	def config = new Yaml().load("""
installations:
  GRADLE-4.3.1:
   type: gradle
   home: /user/share/gradle-4.3.1
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.plugins.gradle.GradleInstallation.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'GRADLE-4.3.1'}
	assert installation.home == '/user/share/gradle-4.3.1'
}

testToolAutoInstaller()
testToolManualInstaller()
