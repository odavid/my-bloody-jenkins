## Changelog

## Known issues:
> FARGATE:
I've added the support needed for ecs plugin configuration to use FARGATE, however, at this moment, it seems the plugin is not working with FARGATE due to missing executionRole field within the plugin.
[I've created a PR for the plugin maintainers](https://github.com/jenkinsci/amazon-ecs-plugin/pull/62) and waiting for the official release.
In any case, the ecs plugin does work as before, just not FARGATE launch type.

## 2.107.3-51
* Updated plugins:
    * [mask-passwords:2.12.0](https://plugins.jenkins.io/mask-passwords)
    * [git:3.9.1](https://plugins.jenkins.io/git)
    * [github:1.29.1](https://plugins.jenkins.io/github)
    * [github-branch-source:2.3.5](https://plugins.jenkins.io/github-branch-source)
    * [kubernetes:1.7.1](https://plugins.jenkins.io/kubernetes)    

## 2.107.3-50
* Updated plugins:
    * [gitlab-plugin:1.5.6](https://plugins.jenkins.io/gitlab-plugin)
    * [aws-java-sdk:1.11.329](https://plugins.jenkins.io/aws-java-sdk)
    * [kubernetes:1.7.0](https://plugins.jenkins.io/kubernetes) 
    
## 2.107.3-49
* added taskrole [#97](https://github.com/odavid/my-bloody-jenkins/pull/97)

## 2.107.3-48
* Added fargate support to ecs cloud [#96](https://github.com/odavid/my-bloody-jenkins/pull/96)
* Updated plugins:
    * [amazon-ecs:1.15](https://plugins.jenkins.io/amazon-ecs)
    * [apache-httpcomponents-client-4-api:4.5.5-2.1](https://plugins.jenkins.io/apache-httpcomponents-client-4-api)
    * [favorite:2.3.2](https://plugins.jenkins.io/favorite)
    * [kubernetes:1.6.2](https://plugins.jenkins.io/kubernetes)

## 2.107.3-47
* Updated plugins:
    * [checkmarx:8.70.0](https://plugins.jenkins.io/checkmarx)
    * [cucumber-reports:3.17.0](https://plugins.jenkins.io/cucumber-reports)
    * [docker-commons:1.13](https://plugins.jenkins.io/docker-commons)
    * [docker-workflow:1.17](https://plugins.jenkins.io/docker-workflow)
    * [extensible-choice-parameter:1.6.0](https://plugins.jenkins.io/extensible-choice-parameter)
    * [gatling:1.2.3](https://plugins.jenkins.io/gatling)
    * [jira:3.0.0](https://plugins.jenkins.io/jira)
    * [workflow-multibranch:2.19](https://plugins.jenkins.io/workflow-multibranch)
    * [workflow-step-api:2.15](https://plugins.jenkins.io/workflow-step-api)


## 2.107.3-46
* Update plugins:
    * [cobertura:1.12.1](https://plugins.jenkins.io/cobertura)
    * [docker-commons:1.12](https://plugins.jenkins.io/docker-commons)
    * [docker-workflow:1.16](https://plugins.jenkins.io/docker-workflow)
    * [metrics:3.1.2.12](https://plugins.jenkins.io/metrics)
    * [git:3.9.0](https://plugins.jenkins.io/git)
    * [git-client:2.7.2](https://plugins.jenkins.io/git-client)
    * [jackson2-api:2.8.11.2](https://plugins.jenkins.io/jackson2-api)


## 2.107.3-45
* [LTS-2.107.3](https://jenkins.io/changelog-stable/)
* Update plugins:
    * [pipeline-utility-steps:2.1.0](https://plugins.jenkins.io/pipeline-utility-steps)

## 2.107.2-44
* Update plugins:
    * [timestamper:1.8.10](https://plugins.jenkins.io/timestamper)
* Added plugins:
    * [http_request:1.8.22](https://plugins.jenkins.io/timestamper)

### 2.107.2-43
* Updated plugins
    * [workflow-cps:2.53](https://plugins.jenkins.io/workflow-cps)

### 2.107.2-42
* Updated plugins
    * [groovy-postbuild:2.4.1](https://plugins.jenkins.io/groovy-postbuild)
    * [jira:2.5.2](https://plugins.jenkins.io/jira)
    * [workflow-cps:2.52](https://plugins.jenkins.io/workflow-cps)

### 2.107.2-41
* Environment Variable substitution and ability to remove secret env vars patterns from System Info page [#91](https://github.com/odavid/my-bloody-jenkins/issues/91)

### 2.107.2-40
* Updated plugins
    * [artifactory:2.16.1](https://plugins.jenkins.io/artifactory)
    * [workflow-cps:2.51](https://plugins.jenkins.io/workflow-cps)
    * [workflow-job:2.21](https://plugins.jenkins.io/workflow-job)

### 2.107.2-39
* Add ability to directly run jobDSL Scripts on startup/update [#89](https://github.com/odavid/my-bloody-jenkins/issues/89)
* Updated plugins
    * [apache-httpcomponents-client-4-api:4.5.5-2.0](https://plugins.jenkins.io/apache-httpcomponents-client-4-api)
    * [artifactory:2.16.0](https://plugins.jenkins.io/artifactory)
    * [p4:1.8.10](https://plugins.jenkins.io/p4)

### 2.107.2-38
* Updated plugins
    * [apache-httpcomponents-client-4-api:4.5.5-1.0](https://plugins.jenkins.io/apache-httpcomponents-client-4-api)
    * [cloudbees-bitbucket-branch-source:2.2.11](https://plugins.jenkins.io/cloudbees-bitbucket-branch-source)
    * [kubernetes:1.6.0](https://plugins.jenkins.io/kubernetes)

### 2.107.2-37
* Updated plugins
    * [branch-api:2.0.20](https://plugins.jenkins.io/branch-api)
    * [github-branch-source:2.3.4](https://plugins.jenkins.io/github-branch-source)
    * [scm-api:2.2.7](https://plugins.jenkins.io/scm-api)
    * [workflow-cps:2.49](https://plugins.jenkins.io/workflow-cps)
    * [workflow-job:2.20](https://plugins.jenkins.io/workflow-job)
    * [workflow-multibranch:2.18](https://plugins.jenkins.io/workflow-multibranch)

### 2.107.2-36
* Fixed typo in custom config

### 2.107.2-35
* Updated plugins
    * [docker-plugin:1.1.4](https://plugins.jenkins.io/docker-plugin)
    * [workflow-basic-steps:2.7](https://plugins.jenkins.io/workflow-basic-steps)

### 2.107.2-34
* Support Dynamic credentials [#59](https://github.com/odavid/my-bloody-jenkins/issues/59)
* Support for custom configuration [#86](https://github.com/odavid/my-bloody-jenkins/issues/86)

### 2.107.2-33
* Updated plugins
    * [amazon-ecs:1.14](https://plugins.jenkins.io/amazon-ecs)
    * [git-parameter:0.9.2](https://plugins.jenkins.io/git-parameter)
    * [htmlpublisher:1.16](https://plugins.jenkins.io/htmlpublisher)
    * [job-dsl:1.69](https://plugins.jenkins.io/job-dsl)
    * [kubernetes:1.5.2](https://plugins.jenkins.io/kubernetes)
    * [pipeline-model-definition:1.2.9](https://plugins.jenkins.io/pipeline-model-definition)
    * [script-security:1.44](https://plugins.jenkins.io/script-security)
    * [workflow-job:2.19](https://plugins.jenkins.io/workflow-job)

### 2.107.2-32
* [LTS-2.107.2](https://jenkins.io/changelog-stable/)
* Updated plugins
    * [p4:1.8.9](https://plugins.jenkins.io/p4)
    * [workflow-api:2.27](https://plugins.jenkins.io/workflow-api)
    * [workflow-cps:2.48](https://plugins.jenkins.io/workflow-cps)

### 2.107.1-31
* Updated plugins
    * [checkmarx:8.60.1](https://plugins.jenkins.io/checkmarx)
    * [token-macro:2.5](https://plugins.jenkins.io/token-macro)
    * [blueocean:1.5.0](https://plugins.jenkins.io/blueocean)

### 2.107.1-30
* Updated plugins
    * [amazon-ecs:1.13](https://plugins.jenkins.io/amazon-ecs)
    * [matrix-project:1.13](https://plugins.jenkins.io/matrix-project)
    * [sonar:2.7.1](https://plugins.jenkins.io/sonar)

### 2.107.1-29
* Updated plugins
    * [amazon-ecs:1.12](https://plugins.jenkins.io/amazon-ecs)
    * [badge:1.4](https://plugins.jenkins.io/badge)
    * [branch-api:2.0.19](https://plugins.jenkins.io/branch-api)
    * [cucumber-reports:3.16.0](https://plugins.jenkins.io/cucumber-reports)
    * [gitlab-plugin:1.5.5](https://plugins.jenkins.io/gitlab-plugin)
    * [kubernetes:1.5.1](https://plugins.jenkins.io/kubernetes)
    * [pipeline-model-api:1.2.8](https://plugins.jenkins.io/pipeline-model-api)
    * [pipeline-model-definition:1.2.8](https://plugins.jenkins.io/pipeline-model-definition)
    * [pipeline-model-extensions:1.2.8](https://plugins.jenkins.io/pipeline-model-extensions)
    * [pipeline-stage-tags-metadata:1.2.8](https://plugins.jenkins.io/pipeline-stage-tags-metadata)
    * [sonar:2.7](https://plugins.jenkins.io/sonar)
    * [workflow-cps:2.47](https://plugins.jenkins.io/workflow-cps)
    * [workflow-job:2.18](https://plugins.jenkins.io/workflow-job)

### 2.107.1-28
* Updated plugins
    * [artifactory:2.15.1](https://plugins.jenkins.io/artifactory)
    * [copyartifact:1.39.1](https://plugins.jenkins.io/copyartifact)
    * [kubernetes:1.5](https://plugins.jenkins.io/kubernetes)
    * [p4:1.8.8](https://plugins.jenkins.io/p4)
    * [pipeline-rest-api:2.10](https://plugins.jenkins.io/pipeline-rest-api)
    * [pipeline-stage-view:2.10](https://plugins.jenkins.io/pipeline-stage-view)

### 2.107.1-27
* Updated plugins
    * [htmlpublisher:1.15](https://plugins.jenkins.io/htmlpublisher)
    * [kubernetes:1.4.1](https://plugins.jenkins.io/kubernetes)
    * [rebuild:1.28](https://plugins.jenkins.io/rebuild)
    * [script-security:1.43](https://plugins.jenkins.io/script-security)
    * [token-macro:2.4](https://plugins.jenkins.io/token-macro)

### 2.107.1-26
* Updated plugins
    * [credentials-binding:1.16](https://plugins.jenkins.io/credentials-binding)
    * [email-ext:2.62](https://plugins.jenkins.io/email-ext)
    * [gitlab-plugin:1.5.4](https://plugins.jenkins.io/gitlab-plugin)
    * [groovy-postbuild:2.4](https://plugins.jenkins.io/groovy-postbuild)
    * [kubernetes:1.4](https://plugins.jenkins.io/kubernetes)
    * [mailer:1.21](https://plugins.jenkins.io/mailer)
    * [maven-plugin:3.1.2](https://plugins.jenkins.io/maven-plugin)
    * [p4:1.8.7](https://plugins.jenkins.io/p4)
    * [pipeline-utility-steps:2.0.2](https://plugins.jenkins.io/pipeline-utility-steps)
    * [swarm:3.12](https://plugins.jenkins.io/swarm)
* Adjustment for changes in configuration for [email-ext:2.62](https://plugins.jenkins.io/email-ext)

### 2.107.1-25
* [LTS-2.107.1](https://jenkins.io/changelog-stable/)
* Updated plugins
    * [artifactory:2.15.0](https://plugins.jenkins.io/artifactory)
    * [cloudbees-folder:6.4](https://plugins.jenkins.io/cloudbees-folder)
    * [config-file-provider:2.18](https://plugins.jenkins.io/config-file-provider)
    * [durable-task:1.22](https://plugins.jenkins.io/durable-task)
    * [extensible-choice-parameter:1.5.0](https://plugins.jenkins.io/extensible-choice-parameter)
    * [github-branch-source:2.3.3](https://plugins.jenkins.io/github-branch-source)
    * [kubernetes:1.3.3](https://plugins.jenkins.io/kubernetes)
    * [mask-passwords:2.11.0](https://plugins.jenkins.io/mask-passwords)
    * [promoted-builds:3.1](https://plugins.jenkins.io/promoted-builds)
    * [script-security:1.42](https://plugins.jenkins.io/script-security)

### 2.89.4-24
* Updated plugins:
    * [durable-task:1.21](https://plugins.jenkins.io/durable-task)
    * [kubernetes:1.3.2](https://plugins.jenkins.io/kubernetes)
    * [lockable-resources:2.2](https://plugins.jenkins.io/lockable-resources)
    * [metrics:3.1.2.11](https://plugins.jenkins.io/metrics)
### 2.89.4-23
* Updated plugins:
    * [blueocean-autofavorite:1.2.2](https://plugins.jenkins.io/blueocean-autofavorite)
    * [cucumber-reports:3.15.0](https://plugins.jenkins.io/cucumber-reports)
    * [git:3.8.0](https://plugins.jenkins.io/git)
    * [mercurial:2.3](https://plugins.jenkins.io/mercurial)
    * [p4:1.8.6](https://plugins.jenkins.io/p4)
    * [promoted-builds:3.0](https://plugins.jenkins.io/promoted-builds)
    * [ssh-slaves:1.26](https://plugins.jenkins.io/ssh-slaves)

### 2.89.4-22
* Updated plugins:
    * [anchore-container-scanner:1.0.14](https://plugins.jenkins.io/anchore-container-scanner)
    * [kubernetes:1.3.1](https://plugins.jenkins.io/kubernetes)
    * [swarm:3.10](https://plugins.jenkins.io/swarm)
    * [workflow-api:2.26](https://plugins.jenkins.io/workflow-api)

### 2.89.4-21
* Updated plugins:
    * [checkmarx:8.60.0](https://plugins.jenkins.io/checkmarx)
    * [git-parameter:0.9.1](https://plugins.jenkins.io/git-parameter)
    * [ldap:1.20](https://plugins.jenkins.io/ldap)

### 2.89.4-20
* [LTS-2.89.4](https://jenkins.io/changelog-stable/)
* Updated plugins:
    * [blueocean:1.4.2](https://plugins.jenkins.io/blueocean)
    * [cloudbees-bitbucket-branch-source:2.2.10](https://plugins.jenkins.io/cloudbees-bitbucket-branch-source)
    * [docker-plugin:1.1.3](https://plugins.jenkins.io/docker-plugin)
    * [docker-workflow:1.15.1](https://plugins.jenkins.io/docker-workflow)
    * [durable-task:1.18](https://plugins.jenkins.io/durable-task)
    * [jsch:0.1.54.2](https://plugins.jenkins.io/jsch)
    * [kubernetes:1.2.1](https://plugins.jenkins.io/kubernetes)
    * [pipeline-utility-steps:2.0.1](https://plugins.jenkins.io/pipeline-utility-steps)
    * [structs:1.14](https://plugins.jenkins.io/structs)
    * [workflow-cps:2.45](https://plugins.jenkins.io/workflow-cps)
    * [workflow-durable-task-step:2.19](https://plugins.jenkins.io/workflow-durable-task-step)

### 2.89.3-19

* Updated plugins:
    * [blueocean:1.4.1](https://plugins.jenkins.io/blueocean)
    * [credentials-binding:1.15](https://plugins.jenkins.io/credentials-binding)
    * [gitlab-plugin:1.5.3](https://plugins.jenkins.io/gitlab-plugin)
    * [jackson2-api:2.8.11.1](https://plugins.jenkins.io/jackson2-api)
    * [job-dsl:1.68](https://plugins.jenkins.io/job-dsl)
    * [junit:1.24](https://plugins.jenkins.io/junit)
    * [kubernetes:1.2](https://plugins.jenkins.io/kubernetes)
    * [p4:1.8.5](https://plugins.jenkins.io/p4)
    * [promoted-builds:2.31.1](https://plugins.jenkins.io/promoted-builds)
    * [script-security:1.41](https://plugins.jenkins.io/script-security)
    * [swarm:3.9](https://plugins.jenkins.io/swarm)
    * [workflow-support:2.18](https://plugins.jenkins.io/workflow-support)

### 2.89.3-18

* Updated plugins:
    * [docker-workflow:1.15](https://plugins.jenkins.io/docker-workflow)
    * [jira:2.5](https://plugins.jenkins.io/jira)
    * [kubernetes:1.1.4](https://plugins.jenkins.io/kubernetes)
    * [ldap:1.19](https://plugins.jenkins.io/ldap)
    * [pipeline-model-definition:1.2.7](https://plugins.jenkins.io/pipeline-model-definition)
    * [structs:1.13](https://plugins.jenkins.io/structs)
    * [workflow-cps:2.44](https://plugins.jenkins.io/workflow-cps)

### 2.89.3-17
* Updated plugins:
    * [cloudbees-bitbucket-branch-source:2.2.9](https://plugins.jenkins.io/cloudbees-bitbucket-branch-source)
    * [extensible-choice-parameter:1.4.2](https://plugins.jenkins.io/extensible-choice-parameter)
    * [git-client:2.7.1](https://plugins.jenkins.io/git-client)
    * [jira:2.5.1](https://plugins.jenkins.io/jira)
    * [job-dsl:1.67](https://plugins.jenkins.io/job-dsl)
    * [maven-plugin:3.1](https://plugins.jenkins.io/maven-plugin)
    * [pipeline-build-step:2.7](https://plugins.jenkins.io/pipeline-build-step)
    * [ssh-slaves:1.25.1](https://plugins.jenkins.io/ssh-slaves)

### 2.89.3-16
* Updated plugins:
    * [ant:1.8](https://plugins.jenkins.io/ant)
    * [apache-httpcomponents-client-4-api:4.5.3-2.1](https://plugins.jenkins.io/apache-httpcomponents-client-4-api)
    * [cucumber-reports:3.14.0](https://plugins.jenkins.io/cucumber-reports)
    * [workflow-api:2.25](https://plugins.jenkins.io/workflow-api)
    * [workflow-cps:2.43](https://plugins.jenkins.io/workflow-cps)
    * [workflow-durable-task-step:2.18](https://plugins.jenkins.io/workflow-durable-task-step)
    * [workflow-job:2.17](https://plugins.jenkins.io/workflow-job)
    * [workflow-multibranch:2.17](https://plugins.jenkins.io/workflow-multibranch)
    * [workflow-support:2.17](https://plugins.jenkins.io/workflow-support)

### 2.89.3-15
* [LTS-2.89.3](https://jenkins.io/changelog-stable/)
* Fixed: sometimes /var/run/docker.sock volume was not accessible for jenkins user, when mounted as a volume.
* Updated plugins:
    * [anchore-container-scanner:1.0.13](https://plugins.jenkins.io/anchore-container-scanner)
    * [aws-java-sdk:1.11.264](https://plugins.jenkins.io/aws-java-sdk)
    * [blueocean:1.4.0](https://plugins.jenkins.io/blueocean)
    * [config-file-provider:2.17](https://plugins.jenkins.io/config-file-provider)
    * [credentials-binding:1.14](https://plugins.jenkins.io/credentials-binding)
    * [github:1.29.0](https://plugins.jenkins.io/github)
    * [command-launcher:1.2](https://plugins.jenkins.io/command-launcher)

### 2.89.2-14
* Updated plugins:
    * [cloudbees-folder:6.3](https://plugins.jenkins.io/cloudbees-folder)
    * [docker-commons:1.11](https://plugins.jenkins.io/docker-commons)
    * [parallel-test-executor:1.10](https://plugins.jenkins.io/parallel-test-executor)
    * [ssh-slaves:1.25](https://plugins.jenkins.io/ssh-slaves)
    * [branch-api:2.0.18](https://plugins.jenkins.io/branch-api)
    * [kubernetes:1.1.3](https://plugins.jenkins.io/kubernetes)
    * [pipeline-model-definition:1.2.6](https://plugins.jenkins.io/pipeline-model-definition)
    * [script-security:1.40](https://plugins.jenkins.io/script-security)
    * [swarm:3.8](https://plugins.jenkins.io/swarm)


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
    * [blueocean-autofavorite:1.1.0](https://plugins.jenkins.io/blueocean-autofavorite)
    * [checkmarx:8.42.0](https://plugins.jenkins.io/checkmarx)
    * [display-url-api:2.1.0](https://plugins.jenkins.io/display-url-api)
    * [durable-task:1.16](https://plugins.jenkins.io/durable-task)
    * [gitlab-plugin:1.5.1](https://plugins.jenkins.io/gitlab-plugin)
    * [junit:1.22.2](https://plugins.jenkins.io/junit)
    * [p4:1.8.1](https://plugins.jenkins.io/p4)
    * [pipeline-model-api:1.2.4](https://plugins.jenkins.io/pipeline-model-api)
    * [pipeline-model-definition:1.2.4](https://plugins.jenkins.io/pipeline-model-definition)
    * [pipeline-model-extensions:1.2.4](https://plugins.jenkins.io/pipeline-model-extensions)
    * [pipeline-stage-tags-metadata:1.2.4](https://plugins.jenkins.io/pipeline-stage-tags-metadata)
    * [script-security:1.35](https://plugins.jenkins.io/script-security)
    * [workflow-cps:2.41](https://plugins.jenkins.io/workflow-cps)
    * [workflow-step-api:2.13](https://plugins.jenkins.io/workflow-step-api)

### 2.73.3-3
* add jenkins environment variables section [#45](https://github.com/odavid/my-bloody-jenkins/issues/45)
* Don't create docker group if GID already exist on start [#46](https://github.com/odavid/my-bloody-jenkins/issues/46)

### 2.73.3-2

* Docker cloud: multiple volumes are not working [#44](https://github.com/odavid/my-bloody-jenkins/issues/44)
* Add proxy configuration when Jenkins is running behind a proxy server [#41](https://github.com/odavid/my-bloody-jenkins/issues/41)


### 2.73.3-1

* First release, based on LTS 2.73.3