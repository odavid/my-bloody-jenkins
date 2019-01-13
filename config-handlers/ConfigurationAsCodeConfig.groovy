import org.yaml.snakeyaml.Yaml
import io.jenkins.plugins.casc.yaml.YamlSource
import io.jenkins.plugins.casc.ConfigurationAsCode

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def setup(config) {
    if(config){
        def yamlString = new Yaml().dump(config)
        ByteArrayInputStream bais = new ByteArrayInputStream(yamlString.getBytes('UTF8'))
        def yamlSource = YamlSource.of(bais)
        ConfigurationAsCode.get().configureWith(yamlSource)
    }

}
return this
