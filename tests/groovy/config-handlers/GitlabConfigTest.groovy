import org.yaml.snakeyaml.Yaml

handler = 'Gitlab'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

credsHandler = evaluate(new File("/usr/share/jenkins/config-handlers/CredsConfig.groovy"))

def testGitlab(){
    credsHandler.setup([
        'my-gitlab-a-token': [
            type: 'gitlab-api-token',
            text: 'xxxyyyzzz'
        ]
    ])
 	def config = new Yaml().load("""
useAuthenticatedEndpoint: false
connections:
  my-gitlab-a:
    url: https://gitlab.com
    apiTokenId: my-gitlab-a-token
    clientBuilderId: 'v4'
    ignoreCertificateErrors: true
    connectionTimeout: 20
    readTimeout: 20
""")

    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig)
    def connection = desc.connections[0]
    assert !desc.useAuthenticatedEndpoint
    assert connection.name == 'my-gitlab-a'
    assert connection.url == 'https://gitlab.com'
    assert connection.apiTokenId == 'my-gitlab-a-token'
    assert connection.clientBuilderId == 'v4'
    assert connection.ignoreCertificateErrors
    assert connection.connectionTimeout == 20
    assert connection.readTimeout == 20
}

testGitlab()