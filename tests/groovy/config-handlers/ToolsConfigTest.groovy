import org.yaml.snakeyaml.Yaml

handler = 'Tools'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def assertTool(id, type, home=null, installerType=null, installerId=null, closure=null){
	def desc = jenkins.model.Jenkins.instance.getDescriptor(type)
	def installation = desc.installations.find{it.name == id}
    if(installerType){
        def installer = installation.properties[0].installers[0]
	    assert installerType.isInstance(installer) : "Tool ${id} installer ${installer} is not instanceof ${installerType}"
        if(installerId){
	        assert installer.id == installerId : "Tool ${id} installer ${installer.id} != ${installerId}"
        }
    }
    if(home){
        assert installation.home == home : "Tool ${id} home ${installation.home} != ${home}"
    }
    if(closure){
        closure(installation)
    }
}

def testTools(){
	def config = new Yaml().load("""
oracle_jdk_download:
  username: oracle-user
  password: oracle-password
installations:
  ANT-auto-install:
   type: ant
   installers:
     - id: '1.10.1'
  ANT-manual-install:
   type: ant
   home: /usr/share/ant-1.10.1
  GRADLE-auto-install:
   type: gradle
   installers:
     - id: '4.3.1'
  GRADLE-manual-install:
   type: gradle
   home: /usr/share/gradle-4.3.1
  MAVEN-auto-install:
   type: maven
   installers:
     - id: '3.5.1'
  MAVEN-manual-install:
   type: maven
   home: /usr/share/maven-3.5.1
  JDK-auto-install:
   type: jdk
   installers:
     - id: '9.0.1'
  JDK-manual-install:
   type: jdk
   home: /usr/share/jdk-901
  DEFAULT-XVFB:
   type: xvfb
   home: /usr/local/bin/
  sonar-auto-install:
   type: sonarQubeRunner
   installers:
     - id: '3.0.3.778'
  sonar-manual-install:
   type: sonarQubeRunner
   home: /usr/share/sonar-latest
  XVFB-command-installer:
   type: xvfb
   installers:
     - type: command
       label: command-label
       command: curl -Ssl http://some.web.site
       toolHome: /usr/local/bin/
  XVFB-zip-installer:
   type: xvfb
   installers:
     - type: zip
       label: zip-label
       url: http://some.web.site/my.zip
       subdir: xxx
  golang-1.11:
   type: golang
   installers:
     - id: '1.11'

""")
	configHandler.setup(config)

	def jdkInstaller = jenkins.model.Jenkins.instance.getDescriptor(hudson.tools.JDKInstaller)
	assert jdkInstaller.username == 'oracle-user'
	assert jdkInstaller.password.toString() == 'oracle-password'

    assertTool('ANT-auto-install', hudson.tasks.Ant.AntInstallation, null, hudson.tasks.Ant.AntInstaller, '1.10.1')
    assertTool('ANT-manual-install', hudson.tasks.Ant.AntInstallation, '/usr/share/ant-1.10.1')
    assertTool('GRADLE-auto-install', hudson.plugins.gradle.GradleInstallation, null, hudson.plugins.gradle.GradleInstaller, '4.3.1')
    assertTool('GRADLE-manual-install', hudson.plugins.gradle.GradleInstallation, '/usr/share/gradle-4.3.1')
    assertTool('MAVEN-auto-install', hudson.tasks.Maven, null, hudson.tasks.Maven.MavenInstaller, '3.5.1')
    assertTool('MAVEN-manual-install', hudson.tasks.Maven, '/usr/share/maven-3.5.1')
    assertTool('JDK-auto-install', hudson.model.JDK, null, hudson.tools.JDKInstaller, '9.0.1')
    assertTool('JDK-manual-install', hudson.model.JDK, '/usr/share/jdk-901')
    assertTool('DEFAULT-XVFB', org.jenkinsci.plugins.xvfb.Xvfb, '/usr/local/bin/')
    assertTool('sonar-auto-install', hudson.plugins.sonar.SonarRunnerInstallation, null, hudson.plugins.sonar.SonarRunnerInstaller, '3.0.3.778')
    assertTool('sonar-manual-install', hudson.plugins.sonar.SonarRunnerInstallation, '/usr/share/sonar-latest')
    assertTool('XVFB-command-installer', org.jenkinsci.plugins.xvfb.Xvfb, null, hudson.tools.CommandInstaller, null){
        def installer = it.properties[0].installers[0]
        assert installer.label == 'command-label'
        assert installer.command == 'curl -Ssl http://some.web.site'
        assert installer.toolHome == '/usr/local/bin/'
    }
    assertTool('XVFB-zip-installer', org.jenkinsci.plugins.xvfb.Xvfb, null, hudson.tools.ZipExtractionInstaller, null){	    assertTool('golang-1.11', org.jenkinsci.plugins.golang.GolangInstallation, null, org.jenkinsci.plugins.golang.GolangInstaller, '1.11')
        def installer = it.properties[0].installers[0]
	    assert installer.label == 'zip-label'
	    assert installer.url == 'http://some.web.site/my.zip'
	    assert installer.subdir == 'xxx'
    }
    assertTool('golang-1.11', org.jenkinsci.plugins.golang.GolangInstallation, null, org.jenkinsci.plugins.golang.GolangInstaller, '1.11')
}

testTools()