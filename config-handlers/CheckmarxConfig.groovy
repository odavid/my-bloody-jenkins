def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    config = config ?: [:]
    def desc = jenkins.model.Jenkins.instance.getDescriptor(com.checkmarx.jenkins.CxScanBuilder)
    config.with{
        desc.serverUrl = serverUrl
        desc.username = username
        desc.password = password
        desc.jobGlobalStatusOnError = jobGlobalStatusOnError ? com.checkmarx.jenkins.JobGlobalStatusOnError.valueOf(jobGlobalStatusOnError) : null
        desc.prohibitProjectCreation = asBoolean(prohibitProjectCreation)
        desc.hideResults = asBoolean(hideResults)
        desc.enableCertificateValidation = asBoolean(enableCertificateValidation)
        desc.excludeFolders = excludeFolders
        desc.filterPattern = filterPattern
        desc.forcingVulnerabilityThresholdEnabled = asBoolean(forcingVulnerabilityThresholdEnabled)
        desc.highThresholdEnforcement = asInt(highThresholdEnforcement, null)
        desc.mediumThresholdEnforcement = asInt(mediumThresholdEnforcement, null)
        desc.lowThresholdEnforcement = asInt(lowThresholdEnforcement, null)
        desc.osaHighThresholdEnforcement = asInt(osaHighThresholdEnforcement, null)
        desc.osaMediumThresholdEnforcement = asInt(osaMediumThresholdEnforcement, null)
        desc.osaLowThresholdEnforcement = asInt(osaLowThresholdEnforcement, null)
        desc.scanTimeOutEnabled = asBoolean(scanTimeOutEnabled)
        desc.scanTimeoutDurationInMinutes = asInt(scanTimeoutDurationInMinutes)
    }
    desc.save()
}
return this
