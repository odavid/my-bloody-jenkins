def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def desc = jenkins.model.Jenkins.instance.getDescriptor(hudson.plugins.jira.JiraProjectProperty)
    def sites = config.sites?.collect{ siteConfig ->
        siteConfig.with{
            return new hudson.plugins.jira.JiraSite(
                url ? new URL(url) : null,
                alternativeUrl ? new URL(alternativeUrl) : null,
                username,
                password,
                asBoolean(supportsWikiStyleComment),
                asBoolean(recordScmChanges),
                userPattern,
                asBoolean(updateJiraIssueForAllStatus),
                groupVisibility,
                roleVisibility,
                asBoolean(useHTTPAuth)
            )
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
