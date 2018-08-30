def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def desc = org.thoughtslive.jenkins.plugins.jira.Config.DESCRIPTOR
    def sites = config.sites?.collect{ siteConfig ->
        siteConfig.with{
            def site = new org.thoughtslive.jenkins.plugins.jira.Site(
                    name ?: url,
                    url ? new URL(url) : null,
                    loginType ?: 'BASIC',
                    asInt(timeout, 0)
                )
            site.userName = userName
            site.password = password
            site.secret = secret
            site.readTimeout = asInt(readTimeout, 0)
            site.token = token
            return site
        }
    }
    if(sites){
        def formData = [:] as net.sf.json.JSONObject
        def req = [
            bindJSONToList: {clz, obj -> return sites}
        ] as org.kohsuke.stapler.StaplerRequest
        desc.configure(req, formData)
    }
}
return this
