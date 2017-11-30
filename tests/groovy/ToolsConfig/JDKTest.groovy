import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testToolAutoInstaller(){
	def config = new Yaml().load("""
installations:
  JDK-9:
   type: jdk
   installers:
     - id: '9.0.1'
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.model.JDK.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'JDK-9'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.tools.JDKInstaller
	assert installation.properties[0].installers[0].id == '9.0.1'
}

def testToolManualInstaller(){
	def config = new Yaml().load("""
installations:
  JDK-9:
   type: jdk
   home: /user/share/jdk-901
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.model.JDK.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'JDK-9'}
	assert installation.home == '/user/share/jdk-901'
}

testToolAutoInstaller()
testToolManualInstaller()
