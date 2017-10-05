import org.yaml.snakeyaml.Yaml

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}



return this