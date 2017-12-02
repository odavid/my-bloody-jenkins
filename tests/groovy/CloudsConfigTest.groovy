import org.yaml.snakeyaml.Yaml

handler = 'Clouds'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def assertCloud(id, type, closure){
    def cloud = jenkins.model.Jenkins.instance.clouds.find{it.name == id}
    assert type.isInstance(cloud) : "Cloud ${id} is not instanceof ${type}"
    if(closure){
        closure(cloud)
    }
}

def testClouds(){
	def config = new Yaml().load("""
ecs-cloud:
  type: ecs
  credentialsId: aws-cred
  region: us-east-1
  cluster: ecs-cluster
  connectTimeout: 60
  jenkinsUrl: http://127.0.0.1:8080
  tunnel: 127.0.0.1:8080
  templates:
    - name: ecs-template
      labels:
        - test
        - generic
      image: odavid/jenkins-jnlp-slave:latest
      remoteFs: /home/jenkins
      memory: 4000
      memoryReservation: 2000
      cpu: 512
      jvmArgs: -Xmx1G
      entrypoint: /entrypoint.sh
      logDriver: aws
      dns: 8.8.8.8
      privileged: true
      logDriverOptions:
        optionA: optionAValue
        optionB: optionBValue
      environment:
        ENV1: env1Value
        ENV2: env2Value
      extraHosts:
        extrHost1: extrHost1
        extrHost2: extrHost2
      volumes:
        - /home/xxx
        - /home/bbb:ro
        - /home/ccc:rw
        - /home/yyy:/home/yyy
        - /home/zzz:/home/zzz:ro
        - /home/aaa:/home/aaa:rw
  
""")
    configHandler.setup(config)

    assertCloud('ecs-cloud', com.cloudbees.jenkins.plugins.amazonecs.ECSCloud){
        assert it.credentialsId == 'aws-cred'
        assert it.regionName == 'us-east-1'
        assert it.cluster == 'ecs-cluster'
        assert it.slaveTimoutInSeconds == 60
        assert it.jenkinsUrl == 'http://127.0.0.1:8080'
        assert it.tunnel == '127.0.0.1:8080'
        def template = it.templates[0]
        assert template.templateName == 'ecs-template'
        assert template.label == 'test generic'
        assert template.image == 'odavid/jenkins-jnlp-slave:latest'
        assert template.remoteFSRoot == '/home/jenkins'
        assert template.memory == 4000
        assert template.memoryReservation == 2000
        assert template.cpu == 512
        assert template.jvmArgs == '-Xmx1G'
        assert template.entrypoint == '/entrypoint.sh'
        assert template.logDriver == 'aws'
        assert template.dnsSearchDomains == '8.8.8.8'
        assert template.privileged
        assert ['optionA=optionAValue', 'optionB=optionBValue'] == template.logDriverOptions.collect{ 
            "${it.name}=${it.value}"
        }
        assert ['ENV1=env1Value', 'ENV2=env2Value'] == template.environments.collect{ 
            "${it.name}=${it.value}"
        }
        assert ['extrHost1=extrHost1', 'extrHost2=extrHost2'] == template.extraHosts.collect{ 
            "${it.ipAddress}=${it.hostname}"
        }
        
    }
    
}
testClouds()