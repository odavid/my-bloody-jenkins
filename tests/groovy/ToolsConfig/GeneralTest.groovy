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

testOracleUserPassword()
