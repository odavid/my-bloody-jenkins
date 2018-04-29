import org.yaml.snakeyaml.Yaml

handler = 'JobDSLScripts'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))


def testJobDSLScripts(){
 	def config = new Yaml().load("""
- |
  folder('a-test-folder')
- | 
  job('a-test-folder/a-test-job') {
    scm {
        git('git://github.com/foo/bar.git')
    }
    triggers {
        scm('H/15 * * * *')
    }
    steps {
        maven('-e clean test')
    }
  }
""")
    configHandler.setup(config)
    def folder = jenkins.model.Jenkins.instance.getItem('a-test-folder')
    assert folder
    def job = jenkins.model.Jenkins.instance.getItemByFullName('a-test-folder/a-test-job')
    assert (job instanceof hudson.model.FreeStyleProject)
}

testJobDSLScripts()