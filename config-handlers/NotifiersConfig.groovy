import jenkins.model.Jenkins

import jenkins.plugins.hipchat.*
import jenkins.plugins.hipchat.impl.*
import jenkins.plugins.hipchat.model.*
import jenkins.plugins.hipchat.model.notifications.Notification.Color
import jenkins.plugins.slack.*
import net.sf.json.JSONObject

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
    def mailer = Jenkins.instance.getDescriptor('hudson.tasks.Mailer')
    config.with{
        if(authUser){
            mailer.setSmtpAuth(authUser, authPassowrd?:'')
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
        def reqMap = [:]
        reqMap['ext_mailer_smtp_server'] = host
        reqMap['ext_mailer_smtp_port'] = port?.toString()
        reqMap['ext_mailer_default_suffix'] = defaultSuffix
        reqMap['ext_mailer_charset'] = charset
        reqMap['ext_mailer_default_content_type'] = defaultContentType
        reqMap['ext_mailer_default_subject'] = defaultSubject
        reqMap['ext_mailer_default_body'] = defaultBody
        reqMap['ext_mailer_emergency_reroute'] = emergencyReroute
        reqMap['ext_mailer_default_replyto'] = replyToAddress
        reqMap['ext_mailer_default_presend_script'] = defaultPresendScript
        reqMap['ext_mailer_default_postsend_script'] = defaultPostsendScript
        reqMap['ext_mailer_max_attachment_size'] = maxAttachmentSize
        reqMap['ext_mailer_default_recipients'] = recipientList?.join(',')
        reqMap['ext_mailer_excluded_committers'] = excludedCommitters?.join(',')
        reqMap['ext_mailer_use_list_id'] = listId
        if(requireAdminForTemplateTesting){
            reqMap['ext_mailer_require_admin_for_template_testing'] = 'true'
        }
        if(enableWatching){
            reqMap['ext_mailer_watching_enabled'] = 'true'
        }
        if(enableWatching){
            reqMap['ext_mailer_watching_enabled'] = 'true'
        }
        if(authUser){
            reqMap['ext_mailer_use_smtp_auth'] = 'true'
            reqMap['ext_mailer_smtp_username'] = authUser
            reqMap['ext_mailer_smtp_password'] = authPassowrd
        }
        if(useSsl){
            reqMap['ext_mailer_smtp_use_ssl'] = 'true'
        }
        if(debugMode){
            reqMap['ext_mailer_debug_mode'] = 'true'
        }
        JSONObject formData = [:] as JSONObject
        def req = [
            getParameter: { name -> reqMap[name] },
            hasParameter: {name -> reqMap[name] != null }
        ] as org.kohsuke.stapler.StaplerRequest
        extMail.configure(req, formData)
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