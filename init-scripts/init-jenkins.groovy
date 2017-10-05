def sharedMethods = evaluate(new File("/var/jenkins_home/init.groovy.d/SharedMethods.groovy"))
def generalConfig = evaluate(new File("/var/jenkins_home/init.groovy.d/GeneralConfig.groovy"))
def credsConfig = evaluate(new File("/var/jenkins_home/init.groovy.d/CredsConfig.groovy"))
def securityConfig = evaluate(new File("/var/jenkins_home/init.groovy.d/SecurityConfig.groovy"))
def cloudsConfig = evaluate(new File("/var/jenkins_home/init.groovy.d/CloudsConfig.groovy"))

def jenkinsConfig = sharedMethods.loadYamlConfig('/etc/jenkins-config.yml')

generalConfig.setup(jenkinsConfig.general)
credsConfig.setup(jenkinsConfig.credentials)
securityConfig.setup(jenkinsConfig.security)
cloudsConfig.setup(jenkinsConfig.clouds)
