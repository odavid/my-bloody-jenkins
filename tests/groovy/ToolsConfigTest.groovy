import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testOracleUserPassword(){
	def config = new Yaml().load("""
oracle_jdk_download:
  username: oracle-user
  password: oracle-password
""")

	configHandler.setup(config)
	def jdkInstaller = jenkins.model.Jenkins.instance.getDescriptor(hudson.tools.JDKInstaller)
	assert jdkInstaller.username == 'oracle-user'
	assert jdkInstaller.password.toString() == 'oracle-password'
}

def testMavenToolAutoInstaller(){
	def config = new Yaml().load("""
installations:
  MAVEN-351:
   type: maven
   installers:
     - id: '3.5.1'
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.tasks.Maven.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'MAVEN-351'}
	assert installation.properties && installation.properties[0].installers[0] instanceof hudson.tasks.Maven.MavenInstaller
	assert installation.properties[0].installers[0].id == '3.5.1'
}

def testMavenToolManual(){
	def config = new Yaml().load("""
installations:
  MAVEN-35:
   type: maven
   home: /user/share/maven-3.5.1
""")
	configHandler.setup(config)
	def desc = jenkins.model.Jenkins.instance.getDescriptorByType(hudson.tasks.Maven.DescriptorImpl)
	def installation = desc.installations.find{it.name == 'MAVEN-35'}
	assert installation.home == '/user/share/maven-3.5.1'
}

testOracleUserPassword()
testMavenToolAutoInstaller()
testMavenToolManual()
