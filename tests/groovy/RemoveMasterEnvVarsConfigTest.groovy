import org.yaml.snakeyaml.Yaml

handler = 'RemoveMasterEnvVars'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testEnvVars(){
 	def config = new Yaml().load("""
- '.*PASSWORD.*'
- 'MY_VARIABLE'
- 'SECRET.*'
""")
    hudson.EnvVars.masterEnvVars.put('PASSWORD', 'SomePass')
    hudson.EnvVars.masterEnvVars.put('MY_VARIABLE', 'VAR')
    hudson.EnvVars.masterEnvVars.put('SECRET', 'VAR')
    hudson.EnvVars.masterEnvVars.put('SECRET111', 'VAR')

    assert (hudson.EnvVars.masterEnvVars['PASSWORD'])
    assert (hudson.EnvVars.masterEnvVars['MY_VARIABLE'])
    assert (hudson.EnvVars.masterEnvVars['SECRET'])
    assert (hudson.EnvVars.masterEnvVars['SECRET111'])
    configHandler.setup(config)
    assert !(hudson.EnvVars.masterEnvVars['PASSWORD'])
    assert !(hudson.EnvVars.masterEnvVars['MY_VARIABLE'])
    assert !(hudson.EnvVars.masterEnvVars['SECRET'])
    assert !(hudson.EnvVars.masterEnvVars['SECRET111'])
}

testEnvVars()