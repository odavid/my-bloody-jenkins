import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import jenkins.model.Jenkins
import hudson.util.Secret

def p4PassCred(config) {
    config.with{
        return new org.jenkinsci.plugins.p4.credentials.P4PasswordImpl(
            CredentialsScope.GLOBAL, 
            id, 
            description, 
            p4port,
            trust ? new org.jenkinsci.plugins.p4.credentials.TrustImpl(trust) : null, 
            username, 
            retry?.toString(),
            timeout?.toString(), 
            p4host, 
            password
        )
    }
}

def p4TicketCred(config) {
    config.with{
        return new org.jenkinsci.plugins.p4.credentials.P4TicketImpl(
            CredentialsScope.GLOBAL, 
            id, 
            description, 
            p4port,
            trust ? new org.jenkinsci.plugins.p4.credentials.TrustImpl(trust) : null, 
            username, 
            retry?.toString(),
            timeout?.toString(),
            p4host, 
            password
        )
    }
}

def sshKeyCred(config) {
    def pk = config.privatekey ?: new String(config.base64?.decodeBase64(), 'UTF-8')
    config.with{
        return new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
            id,
            username,
            new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(pk),
            passphrase,
            description
        )
    }
}

def userPassCred(config) {
    config.with{
        return new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            username,
            password
        )
    }
}

def awsCred(config){
    config.with{
        return new AWSCredentialsImpl(
            CredentialsScope.GLOBAL,
            id,
            access_key,
            secret_access_key,
            description
        )
    }
}

def textCred(config){
    config.with{
        return new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            Secret.fromString(text),
        )
    }
}

def certCred(config){
    config.with{
        def secretBytes = com.cloudbees.plugins.credentials.SecretBytes.fromString(base64)
        def keyStoreSource = new com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl.UploadedKeyStoreSource(secretBytes)
        return new com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            password,
            keyStoreSource
        )
    }
}
def gitlabApiToken(config){
    config.with{
        return new com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            Secret.fromString(text),
        )
    }
}

def createOrUpdateCred(cred){
    def currentCreds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        cred.class,
        Jenkins.instance,
        null,
        null
    )
    def globalDomain = Domain.global()
    def credentialsStore = Jenkins.instance.getExtensionList(SystemCredentialsProvider)[0].getStore()
    def currentCred = currentCreds.find{it.id == cred.id}
    if(!currentCred){
        credentialsStore.addCredentials(globalDomain, cred)
    }else{
        credentialsStore.updateCredentials(globalDomain, currentCred, cred)
    }
    credentialsStore.save()
}

def setup(config){
    config = config ?: [:]
    config.collect{k,v ->
        def credConfig = [id: k] << v
        switch(v.type){
            case 'aws':
                return awsCred(credConfig)
            case 'userpass':
                return userPassCred(credConfig)
            case 'text':
                return textCred(credConfig)
            case 'sshkey':
                return sshKeyCred(credConfig)
            case 'cert':
                return certCred(credConfig)
            case 'gitlab-api-token':
                return gitlabApiToken(credConfig)
            case 'p4-pass':
                return p4PassCred(credConfig)
            case 'p4-ticket':
                return p4TicketCred(credConfig)
            default:
                return null
        }
    }.grep().each{
        createOrUpdateCred(it)
    }
}

return this

