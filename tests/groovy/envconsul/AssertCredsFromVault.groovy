def globalDomain = com.cloudbees.plugins.credentials.domains.Domain.global()
def currentCreds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
    org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl,
    jenkins.model.Jenkins.instance,
    null,
    null
)
def cred = currentCreds.find{it.id == 'secret-from-vault'}
assert cred
assert cred.secret.toString() == "very_SECRET"

