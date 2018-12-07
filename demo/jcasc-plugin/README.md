# jcasc-plugin

Shows how to use *My Bloody Jenkins* with [Jenkins Configuration as Code plugin](https://github.com/jenkinsci/configuration-as-code-plugin) configuration style.

In order to use JCaSC instead of *Groovy configuration handlers*, you should set:

`JENKINS_ENV_CONFIG_MODE=jcasc` and use [config.yml](./config.yml).

## Running the demo
docker-compose up -d

* username: `jenkins.admin`
* password: `password`

