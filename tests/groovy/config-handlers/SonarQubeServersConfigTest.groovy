import org.yaml.snakeyaml.Yaml

handler = 'SonarQubeServers'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testSonarQubeServers(){
 	def config = new Yaml().load("""
buildWrapperEnabled: true
installations:
  sonar-server-5.6:
    serverUrl: https://sonar.domain.com
    serverAuthenticationToken: token
    mojoVersion: 5.6
    additionalProperties: abc=abc
    additionalAnalysisProperties: x=y
    triggers:
      skipScmCause: true
      skipUpstreamCause: true
      envVar: ENV_VAR
""")
    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.sonar.SonarGlobalConfiguration)
    assert desc.buildWrapperEnabled
    def sonarServer = desc.installations.find{it.name == 'sonar-server-5.6'}
    assert sonarServer.serverUrl == 'https://sonar.domain.com'
    assert sonarServer.serverAuthenticationToken?.toString() == 'token'
    assert sonarServer.mojoVersion == '5.6'
    assert sonarServer.additionalProperties == 'abc=abc'
    assert sonarServer.additionalAnalysisProperties == 'x=y'
    assert sonarServer.triggers.skipScmCause
    assert sonarServer.triggers.skipUpstreamCause
    assert sonarServer.triggers.envVar == 'ENV_VAR'
}

testSonarQubeServers()