import org.yaml.snakeyaml.Yaml

def loadProperties(){
    Properties envProperties = new Properties()
    File propertiesFile = new File('/tmp/jenkins-env.properties')
    propertiesFile.withInputStream {
        envProperties.load(it)
    }
    envProperties    
}

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}



return this