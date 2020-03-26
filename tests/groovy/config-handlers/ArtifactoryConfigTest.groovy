import org.yaml.snakeyaml.Yaml

handler = 'Artifactory'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testArtifactory(){
 	def config = new Yaml().load("""
useCredentialsPlugin: true
artifactoryServers:
- serverId: artifactory1
  artifactoryUrl: http://artifactory1
  bypassProxy: true
  connectionRetry: 10
  timeout: 200
  deployerCredentialsConfig:
    credentialsId: 'cred1'
    overridingCredentials: true
  resolverCredentialsConfig:
    username: username1
    password: password1
- serverId: artifactory2
  artifactoryUrl: http://artifactory2
  deployerCredentialsConfig:
    credentialsId: 'cred2'
  resolverCredentialsConfig:
    username: username2
    password: password2
""")

    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(org.jfrog.hudson.ArtifactoryBuilder)
    assert desc.useCredentialsPlugin
    assert desc.artifactoryServers.size() == 2
    def server = desc.artifactoryServers[0]
    assert server.name == 'artifactory1'
    assert server.url == 'http://artifactory1'
    assert server.bypassProxy
    assert server.connectionRetry == 10
    assert server.timeout == 200
    assert server.deployerCredentialsConfig.credentialsId == 'cred1'
    assert server.deployerCredentialsConfig.overridingCredentials
    assert server.resolverCredentialsConfig.username == 'username1'
    assert server.resolverCredentialsConfig.password.toString() == 'password1'
}

testArtifactory()