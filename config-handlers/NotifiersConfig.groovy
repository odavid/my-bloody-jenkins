import jenkins.model.Jenkins

import jenkins.plugins.hipchat.*
import jenkins.plugins.hipchat.impl.*
import jenkins.plugins.hipchat.model.*
import jenkins.plugins.slack.*
import net.sf.json.JSONObject

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value ? value.toBoolean() : defaultValue
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
                n.textFormat, 
                n.notificationType ? NotificationType.valueOf(n.notificationType): null, 
                n.color ? jenkins.plugins.hipchat.model.notifications.Color.valueOf(n.color) : null,
                n.messageTemplate
            )
        }
    }
    return hipchat
}

def slackConfig(config){
    def slack = Jenkins.instance.getDescriptorByType(SlackNotifier.DescriptorImpl)
    config.with{
        JSONObject formData = ['slack': ['tokenCredentialId': credentialId]] as JSONObject
        def params = [
            slackTeamDomain: teamDomain,
            slackBotUser: asBoolean(botUser).toString(),
            slackRoom: room,
            slackBaseUrl: baseUrl,
            slackSendAs: sendAs?:''
        ]
        def req = [
            getParameter: { name -> params[name] }
        ] as org.kohsuke.stapler.StaplerRequest
        slack.configure(req, formData)
    }
    return slack
}

def mailConfig(config){
    def mailer = Jenkins.instance.getDescriptorByType(hudson.tasks.Mailer)
    config.with{
        if(authUser){
            mailer.setSmtpAuth(authUser, authPassowrd?:'')
        }
        mailer.replyToAddress = replyToAddress
        mailer.useSsl = asBoolean(useSsl)
        mailer.smtpHost = host
        mailer.smtpPort = port ? port.toString() : null
        mailer.charset = charset
    }
    return mailer
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