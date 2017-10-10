# My Bloody Jenkins - Self configured Jenkins Docker Image

## Introduction
I've been working a lot with Jenkins/Pipline and Docker in the last couple of years, and wanted to share my experience on these subjects.

Jenkins is great! Jenkins combined with Docker is even greater...

But...

It is HARD to get it work. 

Many small tweaks, plugins that do not work as expected in combination with other plugins, and not to mention configuration and automation.

When it comes to Jenkins combined with Docker it is even harder:

* How to cope with Jenkins master data in a cluster of ECS/Kubernetes? 
    * IP Address is not static...
    * JENKINS_HOME should be available at all time
    * Ephemeral JNLP Docker Slaves can take time to start due to untuned node provisioning strategy 
    * How to keep Docker slaves build docker images
    * Host mounted volumes permissions issues
    * ...

So... Since I spilled some blood on that matter, I've decided to create an ***opinioned*** Jenkins Docker Image that covers some of these subjects... 

Therefore ***My Bloody Jenkins***...

### Main Decisions

* Jenkins does not rely on external configuration management tools. I've decided to make it autopilot docker image, using environment variables that are passed to the container or can be fetched from a centrailized KV store such as Consul.
* I am focusing in docker cloud environment - meaning Jenkins master is running inside docker, slaves are ephemeral docker containers running in Kubernetes/ECS/Swarm clusters
* Using only JNLP slaves and not SSH slaves
* Jenkins master does not have any executer of its own - let the slaves do the actual job
* SCM - Focusing only on git
* Complex configuration items are defined in yaml and can be passed as environment variables
* Plugins
    * Focus on pipeline based plugins
    * Plugins must be baked inside the image and should be treated as a the jenkins binary itself. You need to update/add/remove - Create a new image!
* Focus on the following configuration items:
    * Credentials
        * User/Password
        * SSH Keys
        * Secret Text
        * AWS Credentials
    * Security
        * Jenkins database
        * LDAP
        * Using Project Matrix Authorization Strategy
    * Clouds - As I said above - Only docker based and only JNLP
        * ECS
        * Kubernetes
        * Docker plugin
    * Notifications
        * Email
        * Slack
        * Hipchat
    * Script Approvals - since we are using pipeline, sometimes you must approve some groovy methods (We all ynderstand why it is needed, but this one is bloody...)
    * Tools and installers
        * Apache Ant
        * Apache Maven
        * Gradle
        * JDK
        * Xvfb
        * SonarQube Runner
    * Global Pipeline Libraries - Yes Yes Yes
    * Seed Jobs - As I said, it is an autoplilot Jenkins! We do not want to add/remove/update jobs from the UI. Seed Jobs are also Jenkins pipeline jobs that can use JobDSL scripts to drive the jobs CRUD operations
    * Misc R&D lifecycle tools
        * Checkmarx
        * Jira
        * SonarQube

Ok, enough talking...

## Docker Image Reference
## Environment Variables
The following Environment variables are supported

* ___JENKINS_ENV_ADMIN_USER___ - (mandatory), this variable represents the name of the admin user. If LDAP is your choice of authentication, then this should be a valid LDAP user id. If Using Jenkins Database, then you also need to pass the password of this user within the [configuration](#configuration-reference).
* ___JAVA_OPTS\_*___ - All JAVA_OPTS_ variables will be added to the JAVA_OPTS during startup. Use them to control options (system properties) or memory/gc options
* ___JENKINS_ENV_CONFIG_YAML___ - The [configuration](#configuration-reference) is stored in '/etc/jenkins-config.yml' file. When this variable is set, the contents of this variable is written to the file before starting Jenkins and can be fetched from Consul and also be watched so jenkins can update its configuration everytime this variable is being changed. Since the contents of this variable contains secrets, it is wise to store and pass it from Consul/S3 bucket. In any case, once the file is written, this variable is being unset, so it won't appear in Jenkins 'System Information' page (As I said, blood...)
* ___JENKINS_ENV_HOST_IP___ - When Jenkins is running behind an ELB or a reverse proxy, JNLP slaves must know about the real IP of Jenkins, so they can access the 50000 port. Usually they are using the Jenkins URL to try to get to it, so it is very important to let them know what is the original Jenkins IP Address. If the master has a static IP address, then this variable should be set with the static IP address of the host.
* ___JENKINS_ENV_HOST_IP_CMD___ - Same as ___JENKINS_ENV_HOST_IP___, but this time a shell command expression to fetch the IP Address. In AWS, it is useful to use the EC2 Magic IP: ```JENKINS_ENV_HOST_IP_CMD='curl http://169.254.169.254/latest/meta-data/local-ipv4'```


## Configuration Reference
The '/etc/jenkins-config.yml' file is divided into main configuration sections. Each section is responsible for a specific aspect of jenkins configuration.

### Security Section
Responsible for: 
* Setting up security realm
    * jenkins_database - the adminPassword must be provided
    * ldap - LDAP Configuration must be provided
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
