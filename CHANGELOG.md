## Changelog

<!-- ### Not released yet -->
### 2.89.1 Not released yet
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