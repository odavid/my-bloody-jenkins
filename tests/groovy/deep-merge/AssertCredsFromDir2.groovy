def globalDomain = com.cloudbees.plugins.credentials.domains.Domain.global()
def currentCreds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
    com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl,
    jenkins.model.Jenkins.instance,
    null,
    null
)
def cred = currentCreds.find{it.id == 'git-user-pass'}
assert cred
assert cred.username == "username-dir1"
assert cred.password.toString() == "password-dir2"

