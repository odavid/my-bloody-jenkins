import org.yaml.snakeyaml.Yaml
import com.tikal.hudson.plugins.notification.HudsonNotificationProperty
import com.tikal.hudson.plugins.notification.Endpoint
import com.tikal.hudson.plugins.notification.Protocol
import com.tikal.hudson.plugins.notification.Format

handler = 'SeedJobs'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def assertParam(params, name, type, description, closure){
  def param = params.find{it.name == name}
  assert type.isInstance(param)
  if(description){
    assert param.description == description
  }
  if(closure){
    closure(param)
  }
}

def testSeedJobs(){
 	def config = new Yaml().load("""
my-seed-job:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
  triggers:
    pollScm: H/2 * * * *
    periodic: H/1 * * * *
    artifactory:
      serverId: artifactory
      path: /x/y/z
      schedule: 'H/5 * * * *'
  parameters:
    SIMPLE_STRING: SIMPLE_STRING_VALUE
    SIMPLE_TEXT: |
      SIMPLE_TEXT_LINE1
      SIMPLE_TEXT_LINE2
    SIMPLE_CHOICE:
      - value1
      - value2
    SIMPLE_BOOLEAN: true

    STRING_PARAM:
      type: string
      description: STRING_PARAM
      value: STRING_VALUE
    TEXT_PARAM:
      type: text
      description: TEXT_PARAM
      value: TEXT_VALUE
    CHOICE_PARAM:
      type: choice
      description: CHOICE_PARAM
      choices:
        - value1
        - value2
    BOOLEAN_PARAM:
      type: boolean
      description: BOOLEAN_PARAM
      value: true
    PASSWORD_PARAM:
      type: password
      description: PASSWORD_PARAM
      value: password
  notifications:
  - endpoint: 'http://test:1234'
    event: started
  - endpoint: 'www.test.ru:1235'
    event: failed
    protocol: 'TCP'
    format: 'XML'
    timeout: 600000
    logLines: 1000
my-seed-job-with-concurrent:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
  concurrentBuild: true
/myfolder/my-seed-job-inside-folder:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
/myfolder/mysubfolder/my-seed-job-inside-subfolder:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
/myfolder/mysubfolder/mysubfolder2/my-seed-job-inside-subfolder:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
/myfolder/mysubfolder/mysubfolder2/mysubfolder3/my-seed-job-inside-subfolder:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
/myfolder/mysubfolder/mysubfolder2/mysubfolder3/mysubfolder4/my-seed-job-inside-subfolder:
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    branch: my-test-branch
    credentialsId: my-git-key
  pipeline: src/groovy/Jenkinsfile-config
""")
    configHandler.setup(config)
    def job = jenkins.model.Jenkins.instance.getItem('my-seed-job')
    assert job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob
    assert job.definition.scm.userRemoteConfigs[0].url == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert job.definition.scm.userRemoteConfigs[0].credentialsId == 'my-git-key'
    assert job.definition.scm.branches[0].name == 'my-test-branch'
    assert job.definition.scriptPath == 'src/groovy/Jenkinsfile-config'
    assert !job.concurrentBuild

    def paramsDef = job.getProperty(hudson.model.ParametersDefinitionProperty).parameterDefinitions

    assertParam(paramsDef, 'SIMPLE_STRING', hudson.model.StringParameterDefinition, null){
      assert it.defaultValue == 'SIMPLE_STRING_VALUE'
    }
    assertParam(paramsDef, 'SIMPLE_TEXT', hudson.model.TextParameterDefinition, null){
      assert it.defaultValue == """SIMPLE_TEXT_LINE1
SIMPLE_TEXT_LINE2
"""
    }
    assertParam(paramsDef, 'SIMPLE_CHOICE', hudson.model.ChoiceParameterDefinition, null){
      assert it.choices == ['value1', 'value2']
    }
    assertParam(paramsDef, 'SIMPLE_BOOLEAN', hudson.model.BooleanParameterDefinition, null){
      assert it.defaultValue
    }
    assertParam(paramsDef, 'STRING_PARAM', hudson.model.StringParameterDefinition, 'STRING_PARAM'){
      assert it.defaultValue == 'STRING_VALUE'
    }
    assertParam(paramsDef, 'TEXT_PARAM', hudson.model.TextParameterDefinition, 'TEXT_PARAM'){
      assert it.defaultValue == "TEXT_VALUE"
    }
    assertParam(paramsDef, 'CHOICE_PARAM', hudson.model.ChoiceParameterDefinition, 'CHOICE_PARAM'){
      assert it.choices == ['value1', 'value2']
    }
    assertParam(paramsDef, 'BOOLEAN_PARAM', hudson.model.BooleanParameterDefinition, 'BOOLEAN_PARAM'){
      assert it.defaultValue
    }
    assertParam(paramsDef, 'PASSWORD_PARAM', hudson.model.PasswordParameterValue, 'PASSWORD_PARAM'){
      assert it.value.toString() == 'password'
    }

    def notificationsEndpoints = job.getProperty(HudsonNotificationProperty).getEndpoints()
    assert notificationsEndpoints[0] as String == 'HTTP:http://test:1234'
    assert notificationsEndpoints[0].event == 'started'
    assert notificationsEndpoints[0].protocol as String == 'HTTP'
    assert notificationsEndpoints[0].format as String == 'JSON'
    assert notificationsEndpoints[0].timeout == 30000
    assert notificationsEndpoints[0].loglines == 0
    assert notificationsEndpoints[1] as String == 'TCP:www.test.ru:1235'
    assert notificationsEndpoints[1].event == 'failed'
    assert notificationsEndpoints[1].protocol as String == 'TCP'
    assert notificationsEndpoints[1].format as String == 'XML'
    assert notificationsEndpoints[1].timeout == 600000
    assert notificationsEndpoints[1].loglines == 1000

    def jobconcurrent = jenkins.model.Jenkins.instance.getItem('my-seed-job-with-concurrent')
    assert jobconcurrent instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob
    assert jobconcurrent.definition.scm.userRemoteConfigs[0].url == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert jobconcurrent.definition.scm.userRemoteConfigs[0].credentialsId == 'my-git-key'
    assert jobconcurrent.definition.scm.branches[0].name == 'my-test-branch'
    assert jobconcurrent.definition.scriptPath == 'src/groovy/Jenkinsfile-config'
    assert jobconcurrent.concurrentBuild

    def jobInsideFolder = jenkins.model.Jenkins.instance.getItemByFullName('/myfolder/my-seed-job-inside-folder')
    assert jobInsideFolder instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob

    def jobInsideSubFolder = jenkins.model.Jenkins.instance.getItemByFullName('/myfolder/mysubfolder/my-seed-job-inside-subfolder')
    assert jobInsideSubFolder instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob

    def jobInsideSubFolder2 = jenkins.model.Jenkins.instance.getItemByFullName('/myfolder/mysubfolder/mysubfolder2/my-seed-job-inside-subfolder')
    assert jobInsideSubFolder2 instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob

    def jobInsideSubFolder3 = jenkins.model.Jenkins.instance.getItemByFullName('/myfolder/mysubfolder/mysubfolder2/mysubfolder3/my-seed-job-inside-subfolder')
    assert jobInsideSubFolder3 instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob

    def jobInsideSubFolder4 = jenkins.model.Jenkins.instance.getItemByFullName('/myfolder/mysubfolder/mysubfolder2/mysubfolder3/mysubfolder4/my-seed-job-inside-subfolder')
    assert jobInsideSubFolder4 instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob
}

testSeedJobs()
