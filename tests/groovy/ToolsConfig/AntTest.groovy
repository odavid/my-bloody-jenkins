import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testToolAutoInstaller(){
	def config = new Yaml().load("""
installations:
  ANT-1.10.1:
   type: ant
   installers:
     - id: '1.10.1'
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.tasks.Ant.AntInstallation.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'ANT-1.10.1'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.tasks.Ant.AntInstaller
	assert installation.properties[0].installers[0].id == '1.10.1'
}

def testToolManualInstaller(){
	def config = new Yaml().load("""
installations:
  ANT-1.10.1:
   type: ant
   home: /user/share/ant-1.10.1
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.tasks.Ant.AntInstallation.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'ANT-1.10.1'}
	assert installation.home == '/user/share/ant-1.10.1'
}

testToolAutoInstaller()
testToolManualInstaller()
