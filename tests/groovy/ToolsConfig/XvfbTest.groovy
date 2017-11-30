import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testToolManualInstaller(){
	def config = new Yaml().load("""
installations:
  DEFAULT-XVFB:
   type: xvfb
   home: /usr/local/bin/
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.xvfb.Xvfb.XvfbBuildWrapperDescriptor)
	def installation = desc.installations.find{it.name == 'DEFAULT-XVFB'}
	assert installation.home == '/usr/local/bin/'
}

testToolManualInstaller()
