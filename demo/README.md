# Demo

Shows how to run jenkins and set it up in an incremental manner.

Running [docker-compose.yml](docker-compose.yml) that contains the following services:
* ldap - a simple openldap server that is prepopluated on startup using [bootstrap.ldif](ldap/bootstrap/custom.ldif)
* ldap-admin - a simple ldap php admin for managing ldap [using ui](https://localhost:6443)
* jenkins - My Bloody jenkins that watches changes from config.yml
* jenkins-swarm - A [jenkins-swarm](https://plugins.jenkins.io/swarm) slave that emulates a static slave


## Prerequisites
* docker for mac/windows
* docker-compose


### Starting up a clean jenkins

```shell
docker-compose up -d

sleep 30
open http://localhost:8080
```

### Adding LDAP Config

```shell
cat config-templates/01-ldap.yml >> config.yml

sleep 30
open http://localhost:8080
```

|username|password|groups|
---|---|--|
|bob.dylan|password|developers, team-leaders
|james.dean|password|developers|
|jenkins.admin|password|jenkins-admins
|jenkins.swarm|password|

### Adding credentials
```shell
cat config-templates/02-credentials.yml >> config.yml

sleep 30
open http://localhost:8080/credentials/
```

### Adding tools
```shell
cat config-templates/03-tools.yml >> config.yml

sleep 30
open http://localhost:8080/configureTools/
```

### Adding docker cloud
```shell
cat config-templates/04-docker-cloud.yml >> config.yml

sleep 30
open http://localhost:8080/configure/
```

### Adding dsl scripts
```shell
cat config-templates/05-dsl-scripts.yml >> config.yml

sleep 30
open http://localhost:8080/
```

