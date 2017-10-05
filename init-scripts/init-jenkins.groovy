def sharedMethods = evaluate(new File("/var/jenkins_home/init.groovy.d/SharedMethods.groovy"))
def generalConfig = evaluate(new File("/var/jenkins_home/init.groovy.d/GeneralConfig.groovy"))

def jenkinsConfig = sharedMethods.loadYamlConfig('/etc/jenkins-config.yml')

generalConfig.setup(jenkinsConfig.general)
