import org.yaml.snakeyaml.Yaml

handler = 'Creds'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def assertCred(id, type, closure){
    def globalDomain = com.cloudbees.plugins.credentials.domains.Domain.global()
    def currentCreds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        type,
        jenkins.model.Jenkins.instance,
        null,
        null
    )
    def cred = currentCreds.find{it.id == id}

    assert cred != null : "Credential ${id} is null"
    if(closure){
        closure(cred)
    }
}

def testCreds(){
    def text = "ABCDEFGE"
    def base64Text = text.bytes.encodeBase64().toString()

	def config = new Yaml().load("""
text-cred:
  type: text
  description: The slace secret token
  text: slack-secret-token
aws-cred:
  type: aws
  access_key: xxxx
  secret_access_key: yyyy
  description: aws description
userpass-cred:
  type: userpass
  username: user
  password: password1234
  description: userpass description
gitlab-api-token-cred:
  type: gitlab-api-token
  text: gitlab-1234
  description: gitlab description
ssh-key-as-text:
  type: sshkey
  description: git-ssh-key
  username: user
  passphrase: password1234
  privatekey: ${text}
ssh-key-as-base64:
  type: sshkey
  description: git-ssh-key
  username: user
  passphrase: password1234
  base64: ${base64Text}
cert-cred:
  type: cert
  description: cert description
  password: secret
  base64: ${base64Text}
""")

    configHandler.setup(config)
    
    assertCred("text-cred", org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl){
        assert it.description == "The slace secret token"
        assert it.secret.toString() == "slack-secret-token"
    }
    assertCred("aws-cred", com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl){
        assert it.description == "aws description"
        assert it.accessKey == "xxxx"
        assert it.secretKey.toString() == "yyyy"
    }
    assertCred("userpass-cred", com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl){
        assert it.description == "userpass description"
        assert it.username == "user"
        assert it.password.toString() == "password1234"
    }
    assertCred("gitlab-api-token-cred", com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl){
        assert it.description == "gitlab description"
        assert it.apiToken.toString() == "gitlab-1234"
    }
    assertCred("ssh-key-as-text", com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey){
        assert it.description == "git-ssh-key"
        assert it.username == "user"
        assert it.passphrase.toString() == "password1234"
        assert it.privateKey == text
    }
    assertCred("ssh-key-as-base64", com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey){
        assert it.description == "git-ssh-key"
        assert it.username == "user"
        assert it.passphrase.toString() == "password1234"
        assert it.privateKey == text
    }
    assertCred("cert-cred", com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl){
        assert it.description == "cert description"
        assert it.password.toString() == "secret"
        assert it.keyStoreSource instanceof com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl.UploadedKeyStoreSource
        assert it.keyStoreSource.keyStoreBytes
    }
}
testCreds()