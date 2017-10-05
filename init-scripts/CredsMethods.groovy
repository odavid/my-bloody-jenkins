import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import jenkins.model.Jenkins


def sshKeyCred(config) {
    config.with{
        return new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
            id, 
            username,
            new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privatekey),
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
    config.collect{k,v -> 
        def credConfig = [id: k] << v
        switch(v.type){
            case 'aws':
                return awsCred(credConfig)
            case 'userpass':
                return userPassCred(credConfig)
            case 'sshkey':
                return sshKeyCred(credConfig)
            default:
                return null
        }
    }.grep().each{
        createOrUpdateCred(it)
    }
}

return this

