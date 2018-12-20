# Running slaves with docker-plugin

The following example runs a Jenkins Master using docker-compose and uses [docker-plugin](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin) to run slaves within it.

Running the example will:
* Run the Jenkins master
* Configure Jenkins with the following:
    * jenkins_database realm (user: admin, password: admin)
    * docker cloud: ***docker-cloud*** with jenkinsci/jnlp-slave:latest slave template
    * maven installer: ***MVN-3.5.0*** that will be installed on every slave when needed
    * Seed Job: ***seed-job*** that will create a ***sample-job*** using jobDSL

## Prerequisites

* docker and docker-compose are installed

# Running the example

In order to run the example
```bash
$ # Clone the repository
$ git clone https://github.com/odavid/my-bloody-jenkins.git
$ cd my-bloody-jenkins/examples/docker
$ # start the server including setup of variables. 
$ # Before that step, you can also add a GIT user credential based on GIT_PRIVATE_KEY to config.yml
$ . ./jenkins_server.sh start
```

Wait until you'll see:
```bash
INFO: Jenkins is fully up and running
```

Open the browser:
```bash
open http://localhost:8080/
```

You should see the seed job running and waiting for a kubernetes slave to be started. Please be patient, pulling the jenkinsci/jnlp-slave:latest may take a while (and it will start the server twice).