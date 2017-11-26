# My Bloody Jenkins - An opinionated Jenkins Docker Image
[![Build Status](https://travis-ci.org/odavid/my-bloody-jenkins.svg?branch=master)](https://travis-ci.org/odavid/my-bloody-jenkins)
[![Docker Stars](https://img.shields.io/docker/stars/odavid/my-bloody-jenkins.svg)](https://hub.docker.com/r/odavid/my-bloody-jenkins/)

## Introduction
I've been working a lot with Jenkins/Pipline and Docker in the last couple of years, and wanted to share my experience on these subjects.

Jenkins is great! Jenkins combined with Docker is even greater...

But...

It is HARD to get it work.

Many small tweaks, plugins that do not work as expected in combination with other plugins, and not to mention configuration and automation.

When it comes to Jenkins combined with Docker it is even harder:

* How to cope with Jenkins master data in a cluster of ECS/Kubernetes?
    * IP Address is not static...
    * JENKINS_HOME contents should be available at all time (Distributed Filesystem/NFS/EFS) - but master workspace should be faster.
    * Ephemeral JNLP Docker Slaves can take time to start due to untuned node provisioning strategy
    * How to keep Docker slaves build docker images
    * Host mounted volumes permissions issues
    * ...

So... Since I spilled some blood on that matter, I've decided to create an ***opinionated*** Jenkins Docker Image that covers some of these subjects...

Therefore ***My Bloody Jenkins***...

### Main Decisions

* Jenkins does not rely on external configuration management tools. I've decided to make it an 'autopilot' docker container, using environment variables that are passed to the container or can be fetched from a centrailized KV store such as Consul.
* I am focusing in docker cloud environment - meaning Jenkins master is running inside docker, slaves are ephemeral docker containers running in Kubernetes/ECS/Swarm clusters
* Only using JNLP slaves and not SSH slaves
* By default, Jenkins master does not have any executer of its own - let the slaves do the actual job
* SCM - Focusing only on git
* Complex configuration items are defined in yaml and can be passed as environment variables
* Plugins
    * Focus on pipeline based plugins
    * Plugins must be baked inside the image and should be treated as a the jenkins binary itself. Need to update/add/remove a plugin?? - Create a new image!
* Focus on the following configuration items:
    * Credentials
        * User/Password
        * SSH Keys
        * Secret Text
        * AWS Credentials
        * Certificiate Credentials
    * Security
        * Jenkins database
        * LDAP
        * ActiveDirectory
        * Using Project Matrix Authorization Strategy
    * Clouds - As I said above - Only docker based and only JNLP
        * ECS
        * Kubernetes
        * Docker plugin
    * Notifications
        * Email
        * Slack
        * Hipchat
    * Script Approvals - since we are using pipeline, sometimes you must approve some groovy methods (We all understand why it is needed, but this one is bloody...)
    * Tools and installers
        * Apache Ant
        * Apache Maven
        * Gradle
        * JDK
        * Xvfb
        * SonarQube Runner
    * Global Pipeline Libraries - Yes yes yes. Use them a lot...
    * Seed Jobs - As I said, it is an 'autoplilot' Jenkins! We do not want to add/remove/update jobs from the UI. Seed Jobs are also Jenkins pipeline jobs that can use JobDSL scripts to drive the jobs CRUD operations
    * Misc R&D lifecycle tools
        * Checkmarx
        * Jira
        * SonarQube
        * Gitlab

Ok, enough talking...

# Examples
* [docker-plugin cloud](examples/docker/) cloud using Docker Plugin cloud with seed job. See [examples/docker](examples/docker/)
* [kubernetes](examples/kubernetes/) cloud using Minikube with seed job. See [examples/kubernetes](examples/kubernetes/)

## Environment Variables
The following Environment variables are supported

* ___JENKINS_ENV_ADMIN_USER___ - (***mandatory***) Represents the name of the admin user. If LDAP is your choice of authentication, then this should be a valid LDAP user id. If Using Jenkins Database, then you also need to pass the password of this user within the [configuration](#configuration-reference).

* ___JAVA_OPTS\_*___ - All JAVA_OPTS_ variables will be appended to the JAVA_OPTS during startup. Use them to control options (system properties) or memory/gc options. I am using few of them by default to tweak some known issues:
    * JAVA_OPTS_DISABLE_WIZARD - disables the Jenkins 2 startup wizard
    * JAVA_OPTS_CSP - Default content security policy for HTML Publisher/Gatling plugins - See [Configuring Content Security Policy](https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy)
    * JAVA_OPTS_LOAD_STATS_CLOCK - This one is sweet (: - Reducing the load stats clock enables ephemeral slaves to start immediately without waiting for suspended slaves to be reaped

* ___JENKINS_ENV_CONFIG_YAML___ - The [configuration](#configuration-reference) is stored in '/etc/jenkins-config.yml' file. When this variable is set, the contents of this variable is written to the file before starting Jenkins and can be fetched from Consul and also be watched so jenkins can update its configuration everytime this variable is being changed. Since the contents of this variable contains secrets, it is wise to store and pass it from Consul/S3 bucket. In any case, once the file is written, this variable is being unset, so it won't appear in Jenkins 'System Information' page (As I said, blood...)

* __JENKINS_ENV_CONFIG_YML_S3_URL__ - An s3://\<bucket\>/\<path-to-yaml-file\> URL that will be used to fetch the configuration and updated jenkins everytime it changes. This is an alternative to ___JENKINS_ENV_CONFIG_YAML__ setup.

* __JENKINS_ENV_CONFIG_YML_S3_DISABLE_WATCH__ - If equals to 'true', then the configuration file will be fetched only at startup, but won't be watched. Default 'false'

* ___JENKINS_ENV_CONFIG_YML_S3_POLLING___ - polling interval in seconds to check if file changed in s3. Default (30)

* ___JENKINS_ENV_HOST_IP___ - When Jenkins is running behind an ELB or a reverse proxy, JNLP slaves must know about the real IP of Jenkins, so they can access the 50000 port. Usually they are using the Jenkins URL to try to get to it, so it is very important to let them know what is the original Jenkins IP Address. If the master has a static IP address, then this variable should be set with the static IP address of the host.

* ___JENKINS_ENV_HOST_IP_CMD___ - Same as ___JENKINS_ENV_HOST_IP___, but this time a shell command expression to fetch the IP Address. In AWS, it is useful to use the EC2 Magic IP: ```JENKINS_ENV_HOST_IP_CMD='curl http://169.254.169.254/latest/meta-data/local-ipv4'```

* __JENKINS_HTTP_PORT_FOR_SLAVES__ - (Default: 8080) Used together with JENKINS_ENV_HOST_IP to construct the real jenkinsUrl for jnlp slaves.

* __JENKINS_ENV_USE_SCRIPT_SECURITY__ - false by default, if true, it enables the [Script Security](https://github.com/jenkinsci/job-dsl-plugin/wiki/Script-Security) for dsl scripts

* __JENKINS_ENV_JENKINS_URL__ - Define the Jenkins root URL in configuration. This can be useful when you cannot run the Jenkins master docker container with host network and you need it to be available to slaves

* __JENKINS_ENV_ADMIN_ADDRESS__ - Define the Jenkins admin email address

## Configuration Reference
The '/etc/jenkins-config.yml' file is divided into main configuration sections. Each section is responsible for a specific aspect of jenkins configuration.

### Environment Variables Section
Responsible for adding global environment variables to jenkins config.
Keys are environment variable names and values are their corresponding values. Note that variables names should be a valid environment variable name.

```yaml
  environment:
    ENV_KEY_NAME1: ENV_VALUE1
    ENV_KEY_NAME2: ENV_VALUE1
    
```


### Security Section
Responsible for:
* Setting up security realm
    * jenkins_database - the adminPassword must be provided
    * ldap - LDAP Configuration must be provided
    * active_directory - Uses [active-directory plugin](https://wiki.jenkins.io/display/JENKINS/Active+Directory+plugin)
* User/Group Permissions dict - Each key represent a user or a group and its value is a list of Jenkins [Permissions IDs](https://wiki.jenkins.io/display/JENKINS/Matrix-based+security)

```yaml
# jenkins_database - adminPassword must be provided
security:
    realm: jenkins_database
    adminPassword: S3cr3t
```

```yaml
# ldap - ldap configuration must be provided
security:
    realm: ldap
    server: myldap.server.com:389 # mandatory
    rootDN: dc=mydomain,dc=com # mandatory
    managerDN: cn=search-user,ou=users,dc=mydomain,dc=com # mandatory
    managerPassword: <passowrd> # mandatory
    userSearchBase: ou=users
    userSearchFilter: uid={0}
    groupSearchBase: ou=groups
    groupSearchFilter: cn={0}
    ########################
    # Only one is mandatory - depends on the group membership strategy
    # If a user record contains the groups, then we need to set the
    # groupMembershipAttribute.
    # If a group contains the users belong to it, then groupMembershipFilter
    # should be set.
    groupMembershipAttribute: group
    groupMembershipFilter: memberUid={1}
    ########################
    disableMailAddressResolver: false # default = false
    connectTimeout: 5000 # default = 5000
    readTimeout: 60000 # default = 60000
    displayNameAttr: cn
    emailAttr: email
```

```yaml
# active_directory - active_directory configuration must be provided
security:
    realm: active_directory
    domains:
      - name: corp.mydomain.com
        servers:
          - dc1.corp.mydomain.com
          - dc2.corp.mydomain.com
        site: optional-site
        bindName: CN=user,OU=myorg,OU=User,DC=mydoain,DC=com
        bindPassword: secret
    groupLookupStrategy: AUTO # AUTO, RECURSIVE, CHAIN, TOKENGROUPS
    removeIrrelevantGroups: false
    cache:
      size: 500
      ttl: 30
    startTls: false
    tlsConfiguration: TRUST_ALL_CERTIFICATES # TRUST_ALL_CERTIFICATES, JDK_TRUSTSTORE
    jenkinsInternalUser: my-none-ad-user #
```

```yaml
# Permissions - each key represents a user/group and has list of Jenkins Permissions
security:
    realm: ...
    permissions:
        authenticated: # Special group
            - hudson.model.Hudson.Read # Permission Id - see
            - hudson.model.Item.Read
            - hudson.model.Item.Discover
            - hudson.model.Item.Cancel
        junior-developers:
            - hudson.model.Item.Build
```

### Tools Section
Responsible for:
* Setting up tools locations and auto installers

The following tools are currently supported:
* JDK - (type: jdk)
* Apache Ant (type: ant)
* Apache Maven (type: maven)
* Gradle (type: gradle)
* Xvfb (type: xvfb)
* SonarQube Runner (type: sonarQubeRunner)

The following auto installers are currently supported:
* Oracle JDK installers
* Maven/Gradle/Ant/SonarQube version installers
* Shell command installers (type: command)
* Remote Zip/Tar files installers (type: zip)

The tools section is a dict of tools. Each key represents a tool location/installer ID.
Each tools should have either home property or a list of installers property.

Note: For Oracle JDK Downloaders to work, the oracle download user/password should be provided as well.

```yaml
tools:
  oracle_jdk_download: # The oracle download user/password should be provided
    username: xxx
    password: yyy
  installations:
    JDK8-u144: # The Tool ID
      type: jdk
      installers:
        - id: jdk-8u144-oth-JPR # The exact oracle version id
    JDK7-Latest:
      type: jdk
      home: /usr/java/jdk7/latest # The location of the jdk
    MAVEN-3.1.1:
      type: maven
      home: /usr/share/apache-maven-3.1.1
    MAVEN-3.5.0:
      installers:
        - id: '3.5.0' # The exact maven version to be downloaded
    ANT-1.9.4:
      type: ant
      home: /usr/share/apache-ant-1.9.4
    ANT-1.10.1:
      type: ant
      installers:
        - id: '1.10.1' # The exact ant version to be downloaded
    GRADLE-4.2.1:
      type: gradle
      ... # Same as the above - home/installers with id: <gradle version>
    SONAR-Default:
      type: sonarQubeRunner
      installers:
        - id: '3.0.3.778'
      ... # Same as the above - home/installers with id: <sonar runner version>

    # zip installer and shell command installers
    ANT-XYZ:
      type: ant
      installers:
        - type: zip
          label: centos7 # nodes labels that will use this installer
          url: http://mycompany.domain/ant/xyz/ant.tar.gz
          subdir: apache-ant-zyz # the sub directoy where the tool exists within the zip file
        - type: command
          label: centos6 # nodes labels that will use this installer
          command: /opt/install-ant-xyz
          toolHome: # the directoy on the node where the tool exists after running the command
```

### Credentials Section
Responsible for:
* Setting up [credentials](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin) to be used later on by pipelines/tools
* Each credential has an id, type, description and arbitary attributes according to its type
* The following types are supported:
    * type: text - [simple secret](https://wiki.jenkins.io/display/JENKINS/Plain+Credentials+Plugin). Mandatory attributes:
        * text - the text to encrypt
    * type: aws - an [aws secret](https://wiki.jenkins.io/display/JENKINS/CloudBees+AWS+Credentials+Plugin). Mandatory attributes:
        * access_key - AWS access key
        * secret_access_key - AWS secret access key
    * type: userpass - a [user/password](https://github.com/jenkinsci/credentials-plugin/blob/master/src/main/java/com/cloudbees/plugins/credentials/impl/UsernamePasswordCredentialsImpl.java) pair. Mandatory attributes:
        * username
        * password
    * type: sshkey - an [ssh private key](https://wiki.jenkins.io/display/JENKINS/SSH+Credentials+Plugin) in PEM format. Mandatory attributes:
        * username
        * privatekey - PEM format text
        * base64 - PEM format text base64 encoded. Mandatory if privatekey is not provided
        * passphrase - not mandatory, but encouraged
    * type: cert - a [Certificate](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin). Mandatory attributes:
      * base64 - the PKCS12 certificate bytes base64 encoded
      * password - not mandatory, but encouraged
    * type: gitlab-api-token - a [Gitlab API token](https://wiki.jenkins.io/display/JENKINS/GitLab+Plugin) to be used with the gitlab plugin
      * text - the api token as text

> `Note: Currently the configuration supports only the global credentials domain.`

```yaml
# Each top level key represents the credential id
credentials:
  slack:
    type: text
    description: The slace secret token
    text: slack-secret-token
  hipchat:
    type: text
    text: hipchat-token
  awscred:
    type: aws
    access_key: xxxx
    secret_access_key: yyyy
  gituserpass:
    type: userpass
    username: user
    password: password1234
  my-gitlab-api-token:
    type: gitlab-api-token
    text: <api-token-of-gitlab>
  gitsshkey:
    type: sshkey
    description: git-ssh-key
    username: user
    passphrase: password1234
    privatekey: | ## This is why I love yaml... So simple to handle text with newlines
      -----BEGIN RSA PRIVATE KEY-----
      Proc-Type: 4,ENCRYPTED
      DEK-Info: AES-128-CBC,B1615A0CA4F11333D058A5A9C4D0144E

      wxr6qxA/gcj+Bf1he0YRaqH2HbvHXIshrXTFq7oet5OeF1oaX4yyejotI9oPXvvM
      X9jzLpPwhdpriuFKJKr9jc+1rto/71QExTYEaAWwfYi1EVb1ERGmG4DMANqBKssO
      FTn01t4wew3d6DPcAIwFBT7gvlJfg0poCxae1fhsXGijMy2YryiU+BcV0BYsM6Lj
      VAfn9+djoxPKTv3wPFZPXVrzSkWG8IFUcLBIKE6hf3xxwV5FPHDSegAnwTBV9sVB
      BkVfAHDkzecEtK3iHqa9QUsW014TTLZ7Rbbzh6mskrGxgjgDXXjbdEYbJDtSES6E
      d2o1nXqJEsDdUrSWAoaViR052KyW8f8n//LEjI1a6aveOQWoWXgPwD9jnp5cPrGv
      mWGrZmhWLh0rx61qG+bBVEfbGmLmbi74jxq1/6vaAF4ChtfBks2mpOMMOKf+bKar
      w1laJKgwFRWO7iYql9dHzny2GXy4z6hjD5g3omEdFyWh3GSW8NNkXyojml7tlIa6
      ln286//PSmN0dLZptULAr8A4Bp+aGgybnla7H2F5/s2mGc39MrodFPFbpjp50d4R
      2x4uV4ofVvVExv5wWSdQ60o7trSvBWqwu4MDm2yWCxUiay8I8EF2iM0etutlWm5N
      V9aD8TTC5zHLbwY7YI0OvOXyCWgI5CW1tTsnoDxR1H0aDByuSL+Q+8I5gxRKdJlb
      fYZ683g8AuTkKilHQ6KINAAUuvMvgSWzOa/BU9L7Xqn99w2WgdheLMkdp6O4dJX4
      f4vFTzms+tBVXwqybac/8HZ4uW163pgnBpH2bVFi7/qyd8sl8TYAODN70R2oLv3c
      HBjzI/078G9B4WkpL99FK9cWsfCleIM+HIGeQ9jPDK/hzAhlKIIQxzIaomhcRZlW
      Oc3+zlioBuQSBVtf1UJMrCFSfr9Gq+lbhsD5k8bscU38d5R8EHDGvicpr/KIwjGn
      piNmO7Nz2KcJaLUX6D1oG0p4ioan7js27eBGVnur73hNySbFycwiTGdZkp1CHM7D
      OH2iOMibQGMGYWoci/SoIOgp54Meq4WkEpV5wwr6aCuTzsgnVJXVuMmBk8dnvyat
      EKflp+2kKCj9uPo/QUoozNAuNzyDJU95E8ZfLyT5G9GCQtlf2EnrFZIeQ040xBQC
      6vGQLodjxIG1X8ejDv69FIW66qyofAWVuwCO6wuCWEdLLZrNNhjyPCnnxv5Kw8oC
      nB8/YDntqKg2GqpDu4s0bzAt0CPbMQydvD5x4AWKYCm4HQIF3qX544yUKd9vuO3l
      5t8JE8l4ETGMieKFpE9YiVbobye8iNRyIYVBuvlk4lq+xifw7i3Crmr/+KB+2ABZ
      8TshgEjw8G7TR7qZjZGONCBJ6ozUNR5ipUANc81AA3AUGilBeC4lLUcvatsixtWz
      6BDHwOYfLbfm8YIxDELMt13f++sxKoed4EjFJu+JIjEZlRomPf9pZEmZwTRVASsc
      CbwJjRc022b7HIsetGBYu76KK/Fs25D5JTZjQ3ylMKwhBjrOT7d8Xm90/6eg4hvE
      4c9bdLH+2Xuc6qv/oBoFzVd19c3DiVfns2/5BohfG+pbNwZUVR1vjP/BVDgwDBc+
      -----END RSA PRIVATE KEY-----

  kubernetes-cert:
    type: cert
    password: secret
    base64: >
      MIIM2QIBAzCCDJ8GCSqGSIb3DQEHAaCCDJAEggyMMIIMiDCCBz8GCSqGSIb3DQEHBq
      CCBzAwggcsAgEAMIIHJQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIPPHR3lAy
      ...
```

### Notifiers Section
Responsible for Configuration of the following notifiers:
* Mail - [Default Mailer](https://wiki.jenkins.io/display/JENKINS/Mailer) and [Email-ext](https://wiki.jenkins.io/display/JENKINS/Email-ext+plugin) plugin
* [Slack plugin](https://wiki.jenkins.io/display/JENKINS/Slack+Plugin)
* [Hipchat plugin](https://wiki.jenkins.io/display/JENKINS/Hipchat+Plugin)

```yaml
notifiers:
  mail:
    host: xxx.mydomain.com
    port: 25
    authUser: test
    autPassword: test
    replyToAddress: no-reply@mydomain.com
    defaultSuffix: '@mydomain.com'
    useSsl: true
    charset: UTF-8
  hipchat:
    server: my.api.hipchat.com
    room: JenkinsNotificationRoom
    sendAs: TestSendAs
    v2Enabled: true
    credentialId: hipchat # should be defined in credentials section
  slack:
    teamDomain: teamDoamin
    botUser: true
    room: TestRoom
    baseUrl: http://localhost:8080/
    sendAs: JenkinsSlack
    credentialId: slack # should be defined in credentials section
```

### Pipeline Libraries Section
Responsible for setting up [Global Pipeline Libraries](https://wiki.jenkins.io/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin)

Configuration is composed of dicts where each top level key is the name of the library and its value contains the library configuration (source, default version)
```yaml
pipeline_libraries:
  my-library: # the library name
    source:
      remote: git@github.com:odavid/jenkins-docker.git
      credentialsId: gitsshkey # should be defined in credentials section
    defaultVersion: master
    implicit: false # Default false - if true the library will be available within all pipeline jobs with declaring it with @Library
    allowVersionOverride: true # Default true, better to leave it as is
    includeInChangesets: true # see https://issues.jenkins-ci.org/browse/JENKINS-41497

  my-other-lib:
    source:
      remote:
    ...

```

### Script Approval Section
Responsible for configuration list of white-listed methods to be used within your pipeline groovy scripts. See [Script Security](https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin)

Contains list of method/field signatures that will be added to the [Script Approval Console](https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin)

```yaml
script_approval:
  approvals:
    - field hudson.model.Queue$Item task
    - method groovy.lang.Binding getVariable java.lang.String
    - method groovy.lang.Binding getVariables
    - method groovy.lang.Binding hasVariable java.lang.String
    - method hudson.model.AbstractCIBase getQueue
    - staticMethod java.lang.Math max int int
    - staticMethod java.lang.Math max long long
    - staticMethod java.lang.Math min int int
    - staticMethod java.lang.Math min long long
    - staticMethod java.lang.Math pow double double
    - staticMethod java.lang.Math sqrt double
```

### Clouds Section
Responsible for configuration of the following docker cloud providers:
* Docker - [type: docker](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin)
* Amazon ECS - [type: ecs](https://wiki.jenkins.io/display/JENKINS/Amazon+EC2+Container+Service+Plugin)
* Kubernetes - [type: kubernetes](https://wiki.jenkins.io/display/JENKINS/Kubernetes+Plugin)

You can define multiple clouds. Each cloud is configured as a dict. For top level key represents the cloud name. Each dict has a mandatory type attritube, and a section of templates representing slave docker templates that correspond to list of labels.

#### Amazon ECS
```yaml
clouds:
  # Top level key -> name of the cloud
  ecs-cloud:
    # type is mandatory
    type: ecs
    # If your jenkins master is running on EC2 and is using IAM Role, then you can
    # discard this credential, otherwise, you need to have an
    # aws credential declared in the credentials secion
    credentialsId: 'my-aws-key'
    # AWS region where your ECS Cluster reside
    region: eu-west-1
    # ARN of the ECS Cluster
    cluster: 'arn:ssss'
    # Timeout (in second) for ECS task to be created, usefull if you use large docker
    # slave image, because the host will take more time to pull the docker image
    # If empty or <= 0, the 900 is the default.
    connectTimeout: 0
    # List of templates
    templates:
      - name: ecsSlave
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use
        # this slave template
        labels:
          - ecs-slave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # ECS memory reservation
        memoryReservation: 2048
        # ECS cpu reservation
        cpu: 1024
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx
```


#### Kubernetes
```yaml
clouds:
  # Top level key -> name of the cloud
  kube-cloud:
    # type is mandatory
    type: kubernetes
    # Kubernetes URL
    serverUrl: http://mykubernetes
    # Default kubernetes namespace for slaves
    namespace: jenkins
    # Pod templates
    templates:
      - name: kubeslave
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use this slave template
        labels:
          - kubeslave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx
```

#### Docker Cloud
```yaml
clouds:
  # Top level key -> name of the cloud
  docker-cloud:
    # type is mandatory
    type: docker
    templates:
      - name: dockerslave
        ## How many containers can run at the same time
        instanceCap: 100
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use this slave template
        labels:
          - dockerslave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx
```


### Seed Jobs Section
Responsible for seed job creation and execution. Each seed job would be a pipeline script that can use [jobDsl pipeline step](https://github.com/jenkinsci/job-dsl-plugin/wiki/User-Power-Moves#use-job-dsl-in-pipeline-scripts)

```yaml
seed_jobs:
  # Each top level key is the seed job name
  SeedJob:
    source:
      # git repo where of the seed job
      remote: git@github.com:odavid/my-bloody-jenkins.git
      credentialsId: gitsshkey
      branch: 'master'
    triggers:
      # scm polling trigger
      pollScm: '* * * * *'
      # period trigger
      periodic: '* * * * *'
    # Location of the pipeline script within the repository
    pipeline: example/SeedJobPipeline.groovy
    # always - will be executed everytime the config loader will run
    # firstTimeOnly - will be executed only if the job was not exist
    # never - don't execute the job, let the triggers do their job
    executeWhen: always #firstTimeOnly always never

    # Define parameters with default values to the seed job
    parameters:
      a_boolean_param: # the name of the param
        type: boolean
        value: true
      a_string_param:
        type: string
        value: The String default value
      a_password_param:
        type: password
        value: ThePasswordValue
      a_choice_param:
        type: choice
        choices:
          - choice1 ## The first choice is the default one
          - choice2
          - choice3
      a_text_param:
        type: text
        value: |
          A text with
          new lines

```

### Running Jenkins behind proxy
When running Jenkins behind a proxy server, add the following to your config yaml file:

```yaml

proxy:
  proxyHost: <proxy_address>
  port: <port>
  username: <proxy_auth> # leave empty if not required
  password: <proxy_auth> # leave empty if not required
  noProxyHost: <comma delimited>

```