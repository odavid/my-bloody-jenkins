# Demo

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

