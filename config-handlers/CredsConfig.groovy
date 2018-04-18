import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import jenkins.model.Jenkins
import hudson.util.Secret
import org.jenkinsci.plugins.structs.describable.DescribableModel

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def p4PassCred(config) {
    config.with{
        def p4cred = new org.jenkinsci.plugins.p4.credentials.P4PasswordImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            p4port,
            asBoolean(ssl) ? new org.jenkinsci.plugins.p4.credentials.TrustImpl(trust) : null,
            username,
            retry?.toString(),
            timeout?.toString(),
            p4host,
            password
        )
        p4cred.allhosts = asBoolean(allhosts)
        return p4cred
    }
}

def p4TicketCred(config) {
    config.with{
        return new org.jenkinsci.plugins.p4.credentials.P4TicketImpl(
            CredentialsScope.GLOBAL,
            id,
            description,
            p4port,
            asBoolean(ssl) ? new org.jenkinsci.plugins.p4.credentials.TrustImpl(trust) : null,
            username,
            retry?.toString(),
            timeout?.toString(),
            p4host,
            new org.jenkinsci.plugins.p4.credentials.TicketModeImpl(
                ticketValue ? 'ticketValueSet' : ticketPath ? 'ticketPathSet' : null,
                ticketValue,
                ticketPath
            )
        )
    }
}

def sshKeyCred(config) {
    config.with{
        def keySource = null
        if(fileOnMaster){
            keySource = new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(fileOnMaster)
        }else if(privatekey || base64){
            keySource = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(
                privatekey ?: new String(base64?.decodeBase64(), 'UTF-8')
            )
        }
        return new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
            id,
            username,
            keySource,
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
            access_key ?: accessKey,
            secret_access_key ?: secretKey,
            description,
            iamRoleArn,
            iamMfaSerialNumber?.toString()
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
        def keyStoreSource = null
        if(base64){
            def secretBytes = com.cloudbees.plugins.credentials.SecretBytes.fromString(base64)
            keyStoreSource = new com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl.UploadedKeyStoreSource(secretBytes)
        }else if(fileOnMaster){
            keyStoreSource = new com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl.FileOnMasterKeyStoreSource(fileOnMaster)
        }
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

def tryCreateDynamicCred(config){
    def type = config.type
    def klass
    if(type.contains('.')){
        try{
            klass = Class.forName(type)
        }catch(e){
            println "Could not find class: $type, skipping credential ${config.id} config"
            return null
        }
    }else{
        def matchedDescriptor = Jenkins.get().getDescriptorList(com.cloudbees.plugins.credentials.Credentials)
            .find{ it.klass.toJavaClass().simpleName.toLowerCase().startsWith(type.toLowerCase()) }
        if(!matchedDescriptor){
            println "Could not find class for symobol: $type, skipping credential ${config.id} config"
            return null
        }
        klass = matchedDescriptor.klass.toJavaClass()
    }
    DescribableModel model
    try{
        model = DescribableModel.of(klass)
    }catch(e){
        println "Could not find DescribableModel for: $klass, skipping credential ${config.id} config"
        return null
    }
    try{
        config['scope'] = 'GLOBAL'
        return model.instantiate(config)
    }catch(e){
        println "Could not instantiate DescribableModel for: $klass, skipping credential ${config.id} config"
        e.printStackTrace(out)
        return null
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
                return tryCreateDynamicCred(credConfig)
        }
    }.grep().each{
        createOrUpdateCred(it)
    }
}

return this

