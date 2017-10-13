# Running slaves in Kubernetes

The following examples runs a Jenkins Master using docker-compose and uses [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/) to run slaves within it.

Running the example will:
* Run the Jenkins master
* Configure Jenkins with the following:
    * jenkins_database realm (user: admin, password: admin)
    * certificate credential: ***minikube-cert*** that will enable Jenkins connect with the Minikube cluster
    * kubernetes cloud: ***kube-cloud*** with jenkinsci/jnlp-slave:latest slave template
    * maven installer: ***MVN-3.5.0*** that will be installed on every slave when needed
    * Seed Job: ***seed-job*** that will create a ***sample-job*** using jobDSL

## Prerequisites

* docker-compose is installed
* Minikube installed - https://kubernetes.io/docs/getting-started-guides/minikube/#installation

# Running the example

In order to run the example
```bash
$ # Clone the repository
$ git clone https://github.com/odavid/my-bloody-jenkins.git
$ cd my-bloody-jenkins/examples/kubernetes
$ # setup variables to be used by docker-compose
$ . ./setup-env
$ # run the docker-compose
$ docker-compose up -d --build
$ # view logs
$ docker-compose logs -f
```

Wait until you'll see:
```bash
INFO: Jenkins is fully up and running
```

Open the browser:
```bash
open http://localhost:8080/
```

You should see the seed job running and waiting for a kubernetes slave to be started. Please be patient, pulling the jenkinsci/jnlp-slave:latest may take a while.