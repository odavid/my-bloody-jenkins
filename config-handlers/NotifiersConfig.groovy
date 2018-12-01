import jenkins.model.Jenkins

import jenkins.plugins.hipchat.*
import jenkins.plugins.hipchat.impl.*
import jenkins.plugins.hipchat.model.*
import jenkins.plugins.hipchat.model.notifications.Notification.Color
import jenkins.plugins.slack.*
import net.sf.json.JSONObject

def asLong(value, defaultValue=0){
    return value ? value.toLong() : defaultValue
}
def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def hipchatConfig(config){
    def hipchat = Jenkins.instance.getDescriptorByType(HipChatNotifier.DescriptorImpl)
    config.with{
        hipchat.server = server
        hipchat.room = room
        hipchat.sendAs = sendAs ?: "Jenkins"
        hipchat.v2Enabled = asBoolean(v2Enabled)
        hipchat.credentialId = credentialId
        hipchat.cardProvider = cardProvider ?: DefaultCardProvider.class.name
        hipchat.defaultNotifications = defaultNotifications?.collect{ n ->
            return new NotificationConfig(
                asBoolean(n.notifyEnabled),
                asBoolean(n.textFormat),
                n.notificationType ? NotificationType.valueOf(n.notificationType): null,
                n.color ? Color.valueOf(n.color) : null,
                n.icon,
                n.messageTemplate
            )
        }
    }
    return hipchat
}

def slackConfig(config){
    def slack = Jenkins.instance.getDescriptorByType(SlackNotifier.DescriptorImpl)
    config.with{
        slack.baseUrl = baseUrl
        slack.teamDomain = teamDomain
        slack.token = token
        slack.tokenCredentialId = (tokenCredentialId ?: credentialId) // backward compatible
        slack.botUser = asBoolean(botUser)
        slack.room = room
        slack.sendAs = sendAs
    }
    return slack
}

def mailConfig(config){
    def mailer = Jenkins.instance.getDescriptor('hudson.tasks.Mailer')
    config.with{
        if(authUser){
            mailer.setSmtpAuth(authUser, authPassword?:'')
        }
        mailer.replyToAddress = replyToAddress
        mailer.useSsl = asBoolean(useSsl)
        mailer.smtpHost = host
        mailer.smtpPort = port?.toString()
        mailer.charset = charset
        mailer.defaultSuffix = defaultSuffix
    }
    mailExtConfig(config)
    return mailer
}

def mailExtConfig(config){
    def extMail = Jenkins.instance.getDescriptor('hudson.plugins.emailext.ExtendedEmailPublisher')
    config.with{
        extMail.smtpServer = host
        extMail.smtpPort = port?.toString()
        extMail.defaultSuffix = defaultSuffix
        extMail.charset = charset
        extMail.defaultContentType = defaultContentType
        extMail.defaultSubject = defaultSubject
        extMail.defaultBody = defaultBody
        extMail.emergencyReroute = emergencyReroute
        extMail.defaultReplyTo = replyToAddress
        extMail.defaultPresendScript = defaultPresendScript
        extMail.defaultPostsendScript = defaultPostsendScript
        extMail.maxAttachmentSizeMb = asLong(maxAttachmentSize, -1)
        extMail.defaultRecipients = recipientList?.join(',')
        extMail.excludedCommitters = excludedCommitters?.join(',')
        extMail.listId = listId
        extMail.requireAdminForTemplateTesting = asBoolean(requireAdminForTemplateTesting)
        extMail.watchingEnabled = asBoolean(enableWatching)
        if(authUser){
            extMail.smtpUsername = authUser
            extMail.smtpPassword = authPassword
        }
        extMail.useSsl = asBoolean(useSsl)
        extMail.debugMode = asBoolean(debugMode)
    }
    return extMail
}

def setup(config){
    config = config ?: [:]
    config.collect{ k,v ->
        switch(k){
            case 'hipchat':
                return hipchatConfig(v)
            case 'slack':
                return slackConfig(v)
            case 'mail':
                return mailConfig(v)
        }
    }.grep().each{
        it.save()
    }
}

return this