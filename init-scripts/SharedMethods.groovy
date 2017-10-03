
def loadProperties(){
    Properties envProperties = new Properties()
    File propertiesFile = new File('/tmp/jenkins-env.properties')
    propertiesFile.withInputStream {
        envProperties.load(it)
    }
    envProperties    
}
return this