import org.yaml.snakeyaml.Yaml

handler = 'Checkmarx'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testCheckmarx(){
 	def config = new Yaml().load("""
serverUrl: http://my-checkmarx.domain.com
username: checkmarx
password: checkmarx
jobGlobalStatusOnError: FAILURE
prohibitProjectCreation: true
hideResults: true
enableCertificateValidation: true
excludeFolders: xxx 
filterPattern: xxx
forcingVulnerabilityThresholdEnabled: true
highThresholdEnforcement: 1000
mediumThresholdEnforcement: 1000
lowThresholdEnforcement: 1000
osaHighThresholdEnforcement: 1000
osaMediumThresholdEnforcement: 1000
osaLowThresholdEnforcement: 1000
scanTimeOutEnabled: true
scanTimeoutDuration: 1000
""")

    configHandler.setup(config)
    def desc = jenkins.model.Jenkins.instance.getDescriptor(com.checkmarx.jenkins.CxScanBuilder)
    assert desc.serverUrl == 'http://my-checkmarx.domain.com'
    assert desc.username == 'checkmarx'
    assert desc.passwordPlainText == 'checkmarx'
    assert desc.jobGlobalStatusOnError == com.checkmarx.jenkins.JobGlobalStatusOnError.FAILURE
    assert desc.prohibitProjectCreation
    assert desc.hideResults
    assert desc.enableCertificateValidation
    assert desc.excludeFolders == 'xxx'
    assert desc.filterPattern == 'xxx'
    assert desc.forcingVulnerabilityThresholdEnabled
    assert desc.highThresholdEnforcement == 1000
    assert desc.mediumThresholdEnforcement == 1000
    assert desc.lowThresholdEnforcement == 1000
    assert desc.osaHighThresholdEnforcement == 1000
    assert desc.osaMediumThresholdEnforcement == 1000
    assert desc.osaLowThresholdEnforcement == 1000
    assert desc.scanTimeOutEnabled
    assert desc.scanTimeoutDuration == 1000

}

testCheckmarx()