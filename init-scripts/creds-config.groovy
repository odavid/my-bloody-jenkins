import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import jenkins.model.Jenkins



def sshKeyCred(id, username, privateKeyString, passphrase = "", description = null) {
    new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
        id, username,
        new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKeyString),
        passphrase,
        description ?: username
    )
}

def userPassCred(id, username, password, description = null) {
    new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL, 
        id, description ?: username, 
        username, 
        password
    )
}

def createOrUpdateCred(cred){
    def currentCreds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.common.StandardUsernameCredentials,
        Jenkins.instance,
        null,
        null
    )
    def globalDomain = Domain.global()
    def credentialsStore = Jenkins.instance.getExtensionList(SystemCredentialsProvider)[0].getStore()
    def currentCred = currentCreds.find{it.id == cred.id}
    if(!cred){
        credentialsStore.addCredentials(globalDomain, cred)
    }else{
        credentialsStore.updateCredentials(globalDomain, currentCred, cred)
    }
    credentialsStore.save()
}

Properties envProperties = new Properties()
File propertiesFile = new File('/tmp/jenkins-env.properties')
propertiesFile.withInputStream {
    envProperties.load(it)
}

//CRED_USERPASS_EXAMPLE=<id>:<user>:<pass>
def userPassCreds = envProperties.findAll{ it.key.startsWith('CRED_USERPASS_')}

//CRED_SSHKEY_PEM_EXAMPLE="$(cat ~/.ssh/id_rsa)"
def sshPemCreds = envProperties.findAll{ it.key.startsWith('CRED_SSHKEY_PEM_')}
//CRED_SSHKEY_DETAILS_EXAMPLE=<id>:<user>[:<passphrase>]
def sshDetailsCreds = envProperties.findAll{ it.key.startsWith('CRED_SSHKEY_DETAILS_')}

userPassCreds.each{
    def asList = it.value.split(':')
    def id = asList[0]
    def user = asList[1]
    def password = asList[2]
    def cred = userPassCred(id, user, password)
    createOrUpdateCred(cred)
}
sshPemCreds.each{
    def keyName = it.key.substring('CRED_SSHKEY_PEM_'.size())
    def matchingSshCredDetailsKey = "CRED_SSHKEY_DETAILS_${keyName}"
    def matchingSshCredDetails = sshDetailsCreds[matchingSshCredDetailsKey]
    if(matchingSshCredDetails){
        def detailsAsList = matchingSshCredDetails.split(':')
        def id = detailsAsList[0]
        def username = detailsAsList[1]
        def passphrase = detailsAsList.size() > 3 ? detailsAsList[3] : ""
        def cred = sshKeyCred(id, username, it.value, passphrase)
        createOrUpdateCred(cred)
    }else{
        println "Could not find ${matchingSshCredDetailsKey} variable, skipping ssh credential setup"
    }
}
