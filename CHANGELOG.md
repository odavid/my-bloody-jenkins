## Changelog

### 2.89.2-14 (Not released yet)
* Updated plugins:
    * [cloudbees-folder:6.3](https://plugins.jenkins.io/cloudbees-folder)
    * [docker-commons:1.11](https://plugins.jenkins.io/docker-commons)
    * [parallel-test-executor:1.10](https://plugins.jenkins.io/parallel-test-executor)
    * [ssh-slaves:1.25](https://plugins.jenkins.io/ssh-slaves)

### 2.89.2-13
* Users and groups of local jenkins_database security realm should be persistent between restarts [#78](https://github.com/odavid/my-bloody-jenkins/pull/78)
* Updated plugins:
    * [artifactory:2.14.0](https://plugins.jenkins.io/artifactory)
    * [branch-api:2.0.17](https://plugins.jenkins.io/branch-api)
    * [envinject-api:1.5](https://plugins.jenkins.io/envinject-api)
    * [p4:1.8.4](https://plugins.jenkins.io/p4)
    * [pipeline-graph-analysis:1.6](https://plugins.jenkins.io/pipeline-graph-analysis)

### 2.89.2-12
* Using jenkinsUrl in docker-plugin. The plugin is now able to set an alternate jenkinsUrl. This means that the end user can only set JENKINS_ENV_HOST_IP and can define different JENKINS_ENV_JENKINS_URL value.
* Updated plugins
    * [swarm:3.7](https://plugins.jenkins.io/swarm)

### 2.89.2-11
* JENKINS_ENV_QUIET_STARTUP_PERIOD - support for jenkins safe restart
* Updated plugins
    * [git:3.7.0](https://plugins.jenkins.io/git)

### 2.89.2-10
* Changed lts version to [2.89.2](https://jenkins.io/changelog-stable/)
* Updated plugins
    * [aws-java-sdk:1.11.248](https://plugins.jenkins.io/aws-java-sdk)
    * [cloudbees-bitbucket-branch-source:2.2.8](https://plugins.jenkins.io/cloudbees-bitbucket-branch-source)
    * [cucumber-reports:3.13.0](https://plugins.jenkins.io/cucumber-reports)
    * [docker-plugin:1.1.2](https://plugins.jenkins.io/docker-plugin)
    * [git-client:2.7.0](https://plugins.jenkins.io/git-client)
    * [github-branch-source:2.3.2](https://plugins.jenkins.io/github-branch-source)
    * [jackson2-api:2.8.10.1](https://plugins.jenkins.io/jackson2-api)
    * [scm-api:2.2.6](https://plugins.jenkins.io/scm-api)
    * [script-security:1.39](https://plugins.jenkins.io/script-security)
    * [ssh-slaves:1.24](https://plugins.jenkins.io/ssh-slaves)
    * [timestamper:1.8.9](https://plugins.jenkins.io/timestamper)

### 2.89.1-9
* Support of fileOnMaster for credentials type sshkey [#70](https://github.com/odavid/my-bloody-jenkins/issues/70)

### 2.89.1-8
* Added Perforce Credentials:
    * p4-pass - p4 password
    * p4-ticket - p4 ticket
* Support iamMfaSerialNumber and iamRoleArn in aws-cred
* Updated plugins:
    * [blueocean](https://plugins.jenkins.io/blueocean)
    * [docker-commons](https://plugins.jenkins.io/docker-commons)
    * [docker-plugin](https://plugins.jenkins.io/docker-plugin)
    * [script-security](https://plugins.jenkins.io/script-security)

### 2.89.1-7
* Using latest [LTS image 2.89.1](https://jenkins.io/changelog-stable/)
* Upgrade the following plugins:
    * [blueocean](https://plugins.jenkins.io/blueocean)
    * [branch-api](https://plugins.jenkins.io/branch-api)
    * [buildtriggerbadge](https://plugins.jenkins.io/buildtriggerbadge)
    * [docker-plugin](https://plugins.jenkins.io/docker-plugin)
    * [kubernetes](https://plugins.jenkins.io/kubernetes)
    * [lockable-resources](https://plugins.jenkins.io/lockable-resources)
    * [pipeline-build-step](https://plugins.jenkins.io/pipeline-build-step)
    * [workflow-api](https://plugins.jenkins.io/workflow-api)
    * [workflow-job](https://plugins.jenkins.io/workflow-job/workflow-job)
* Added securityOptions config under security section with the following options:
    * preventCSRF - boolean, default: true
    * enableScriptSecurityForDSL - boolean, default: false
    * enableCLIOverRemoting - boolean, default: false
    * enableAgentMasterAccessControl - boolean, default: true
    * disableRememberMe - boolean, default: false
    * sshdEnabled - boolean, default: false
    * jnlpProtocols - list, default: [JNLP4] valid values: [JNLP, JNLP2, JNLP3, JNLP4]
* Sometimes watch-file is stopped without any known reason [#65](https://github.com/odavid/my-bloody-jenkins/issues/65)


* By default jenkins security is configured with:
    * CSRF prevention
    * Master/Agent access control


### 2.73.3-6
* Ability to install plugins that are not baked within image before jenkins starts [#50](https://github.com/odavid/my-bloody-jenkins/issues/50)
* Fixed S3 Config Fetch error
* update-config error during watch

### 2.73.3-5
* Config file should be stored in tmpfs by default for security reasons [#61](https://github.com/odavid/my-bloody-jenkins/pull/61)
* New tags in docker cloud:
    * Added lts tag in docker cloud to point to the latest release tag (my-bloody-jenkins:2.73.3-5 -> my-bloody-jenkins:lts)
    * Added the exact lts version tag to point to the latest release tag (my-bloody-jenkins:2.73.3-5 -> my-bloody-jenkins:2.73.3)

### 2.73.3-4
* When Jenkins starts, sometimes tools dropdown lists are empty [#56](https://github.com/odavid/my-bloody-jenkins/issues/56)
* Generalize S3 Watch to be able to watch other sources [#51](https://github.com/odavid/my-bloody-jenkins/issues/51)
    * **NOTE:** The Following variables were renamed:
        * JENKINS_ENV_CONFIG_YML_S3_URL --> JENKINS_ENV_CONFIG_YML_URL
        * JENKINS_ENV_CONFIG_YML_S3_DISABLE_WATCH --> JENKINS_ENV_CONFIG_YML_URL_DISABLE_WATCH
        * JENKINS_ENV_CONFIG_YML_S3_POLLING --> JENKINS_ENV_CONFIG_YML_URL_POLLING
* docker cloud: Add jnlpUser in template level [#47](https://github.com/odavid/my-bloody-jenkins/issues/47)
* Added [anchore-container-scanner:1.0.12 plugin](https://wiki.jenkins.io/display/JENKINS/Anchore+Container+Image+Scanner+Plugin)
* Updated plugins:
    * blueocean-autofavorite:1.1.0
    * checkmarx:8.42.0
    * display-url-api:2.1.0
    * durable-task:1.16
    * gitlab-plugin:1.5.1
    * junit:1.22.2
    * p4:1.8.1
    * pipeline-model-api:1.2.4
    * pipeline-model-definition:1.2.4
    * pipeline-model-extensions:1.2.4
    * pipeline-stage-tags-metadata:1.2.4
    * script-security:1.35
    * workflow-cps:2.41
    * workflow-step-api:2.13

### 2.73.3-3
* add jenkins environment variables section [#45](https://github.com/odavid/my-bloody-jenkins/issues/45)
* Don't create docker group if GID already exist on start [#46](https://github.com/odavid/my-bloody-jenkins/issues/46)

### 2.73.3-2

* Docker cloud: multiple volumes are not working [#44](https://github.com/odavid/my-bloody-jenkins/issues/44)
* Add proxy configuration when Jenkins is running behind a proxy server [#41](https://github.com/odavid/my-bloody-jenkins/issues/41)


### 2.73.3-1

* First release, based on LTS 2.73.3