## Changelog

### 2.73.3-4 (Not released yet)
* Generalize S3 Watch to be able to watch other sources [#51](https://github.com/odavid/my-bloody-jenkins/issues/51)
    * **NOTE:** The Following variables were renamed:
        * JENKINS_ENV_CONFIG_YML_S3_URL --> JENKINS_ENV_CONFIG_YML_URL
        * JENKINS_ENV_CONFIG_YML_S3_DISABLE_WATCH --> JENKINS_ENV_CONFIG_YML_URL_DISABLE_WATCH
        * JENKINS_ENV_CONFIG_YML_S3_POLLING --> JENKINS_ENV_CONFIG_YML_URL_POLLING

### 2.73.3-3
* add jenkins environment variables section [#45](https://github.com/odavid/my-bloody-jenkins/issues/45)
* Don't create docker group if GID already exist on start [#46](https://github.com/odavid/my-bloody-jenkins/issues/46)

### 2.73.3-2

* Docker cloud: multiple volumes are not working [#44](https://github.com/odavid/my-bloody-jenkins/issues/44)
* Add proxy configuration when Jenkins is running behind a proxy server [#41](https://github.com/odavid/my-bloody-jenkins/issues/41)


### 2.73.3-1

* First release, based on LTS 2.73.3