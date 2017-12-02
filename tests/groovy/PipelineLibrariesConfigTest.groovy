import org.yaml.snakeyaml.Yaml

handler = 'PipelineLibraries'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testLibraries(){
 	def config = new Yaml().load("""
my-lib:
  defaultVersion: master
  implicit: true
  allowVersionOverride: false
  includeInChangesets: false
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    credentialsId: my-git-key
    includes: '*/master'
    excludes: '*/non-master'
    ignoreOnPushNotifications: true
my-lib-with-defaults:
  defaultVersion: master
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    credentialsId: my-git-key
""")

    configHandler.setup(config)
    def myLib = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries.find{ it.name == 'my-lib'}
    assert myLib.defaultVersion == 'master'
    assert myLib.implicit
    assert !myLib.allowVersionOverride
    assert !myLib.includeInChangesets
    assert myLib.retriever.scm.id == 'git-scm-my-lib'
    assert myLib.retriever.scm.remote == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert myLib.retriever.scm.credentialsId == 'my-git-key'
    assert myLib.retriever.scm.includes == '*/master'
    assert myLib.retriever.scm.excludes == '*/non-master'
    assert myLib.retriever.scm.ignoreOnPushNotifications

    myLib = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries.find{ it.name == 'my-lib-with-defaults'}
    assert myLib.defaultVersion == 'master'
    assert !myLib.implicit
    assert myLib.allowVersionOverride
    assert myLib.includeInChangesets
    assert myLib.retriever.scm.id == 'git-scm-my-lib-with-defaults'
    assert myLib.retriever.scm.remote == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert myLib.retriever.scm.credentialsId == 'my-git-key'
    assert myLib.retriever.scm.includes == '*'
    assert myLib.retriever.scm.excludes == ''
    assert !myLib.retriever.scm.ignoreOnPushNotifications
}

testLibraries()