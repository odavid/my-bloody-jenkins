import org.yaml.snakeyaml.Yaml

handler = 'SonarQubeServers'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testSonarQubeServers(){
 	def config = new Yaml().load("""
buildWrapperEnabled: true
installations:
  sonar-server-5.3:
    serverUrl: https://sonar.domain.com
    serverVersion: 5.3
    serverAuthenticationToken: token
    databaseUrl: jdbc:mysql://localhost/test
    databaseLogin: sonar
    databasePassword: sonar
    mojoVersion: 5.3
    additionalProperties: abc=abc
    sonarLogin: sonar
    sonarPassword: sonar
    additionalAnalysisProperties: x=y
    triggers:
      skipScmCause: true
      skipUpstreamCause: true
      envVar: ENV_VAR
  sonar-server-5.1:
    serverUrl: https://sonar.domain.com
    serverVersion: 5.1
    serverAuthenticationToken: token
    databaseUrl: jdbc:mysql://localhost/test
    databaseLogin: sonar
    databasePassword: sonar
    mojoVersion: 5.1
    additionalProperties: abc=abc
    sonarLogin: sonar
    sonarPassword: sonar
    additionalAnalysisProperties: x=y
    triggers:
      skipScmCause: true
      skipUpstreamCause: true
      envVar: ENV_VAR
""")
    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.sonar.SonarGlobalConfiguration)
    assert desc.buildWrapperEnabled
    def sonarServer = desc.installations.find{it.name == 'sonar-server-5.3'}
    assert sonarServer.serverUrl == 'https://sonar.domain.com'
    assert sonarServer.serverVersion == '5.3'
    assert sonarServer.serverAuthenticationToken == 'token'
    assert !sonarServer.databaseUrl
    assert !sonarServer.databaseLogin
    assert !sonarServer.databasePassword
    assert sonarServer.mojoVersion == '5.3'
    assert sonarServer.additionalProperties == 'abc=abc'
    assert !sonarServer.sonarLogin
    assert !sonarServer.sonarPassword
    assert sonarServer.additionalAnalysisProperties == 'x=y'
    assert sonarServer.triggers.skipScmCause
    assert sonarServer.triggers.skipUpstreamCause
    assert sonarServer.triggers.envVar == 'ENV_VAR'

    sonarServer = desc.installations.find{it.name == 'sonar-server-5.1'}
    assert sonarServer.serverUrl == 'https://sonar.domain.com'
    assert sonarServer.serverVersion == '5.1'
    assert !sonarServer.serverAuthenticationToken
    assert sonarServer.databaseUrl == 'jdbc:mysql://localhost/test'
    assert sonarServer.databaseLogin == 'sonar'
    assert sonarServer.databasePassword == 'sonar'
    assert sonarServer.mojoVersion == '5.1'
    assert sonarServer.additionalProperties == 'abc=abc'
    assert sonarServer.sonarLogin == 'sonar'
    assert sonarServer.sonarPassword == 'sonar'
    assert sonarServer.additionalAnalysisProperties == 'x=y'
    assert sonarServer.triggers.skipScmCause
    assert sonarServer.triggers.skipUpstreamCause
    assert sonarServer.triggers.envVar == 'ENV_VAR'
}

testSonarQubeServers()