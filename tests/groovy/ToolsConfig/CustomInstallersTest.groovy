import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testToolCommandInstaller(){
	def config = new Yaml().load("""
installations:
  DEFAULT-XVFB:
   type: xvfb
   installers:
     - type: command
       label: command-label
       command: curl -Ssl http://some.web.site
       toolHome: /usr/local/bin/

""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.xvfb.Xvfb.XvfbBuildWrapperDescriptor)
	def installation = desc.installations.find{it.name == 'DEFAULT-XVFB'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.tools.CommandInstaller
	assert installation.properties[0].installers[0].label == 'command-label'
	assert installation.properties[0].installers[0].command == 'curl -Ssl http://some.web.site'
	assert installation.properties[0].installers[0].toolHome == '/usr/local/bin/'
}

def testToolZipInstaller(){
	def config = new Yaml().load("""
installations:
  DEFAULT-XVFB:
   type: xvfb
   installers:
     - type: zip
       label: zip-label
       url: http://some.web.site/my.zip
       subdir: xxx

""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.xvfb.Xvfb.XvfbBuildWrapperDescriptor)
	def installation = desc.installations.find{it.name == 'DEFAULT-XVFB'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.tools.ZipExtractionInstaller
	assert installation.properties[0].installers[0].label == 'zip-label'
	assert installation.properties[0].installers[0].url == 'http://some.web.site/my.zip'
	assert installation.properties[0].installers[0].subdir == 'xxx'
}

testToolCommandInstaller()
testToolZipInstaller()
