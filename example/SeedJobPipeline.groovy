import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import jenkins.model.Jenkins



def currentCred = SystemCredentialsProvider.instance.credentials.find{it.id == 'gituserpass'}
out.println currentCred.username
out.println currentCred.password.plainText