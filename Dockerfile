FROM jenkins/jenkins:2.138.1-alpine

ARG GOSU_VERSION=1.10

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Using root to install and run entrypoint.
# We will change the user to jenkins using gosu
USER root

# Ability to use usermod + install awscli in order to be able to watch s3 if needed
RUN apk add --no-cache shadow py-setuptools less outils-md5 && \
    easy_install-2.7 pip && \
    pip install --no-cache-dir awscli PyYAML==3.12 six requests botocore boto3

RUN curl -SsL https://releases.hashicorp.com/envconsul/0.7.3/envconsul_0.7.3_linux_amd64.tgz | tar -C /usr/bin -xvzf - && \
    chmod +x /usr/bin/envconsul

RUN curl -SsLo /usr/bin/gosu https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-amd64 && \
     chmod +x /usr/bin/gosu

# Separate between JENKINS_HOME and WORKSPACE dir. Best if we use NFS for JENKINS_HOME
RUN mkdir -p /jenkins-workspace-home && \
    chown -R jenkins:jenkins /jenkins-workspace-home

# Do things on behalf of jenkins user
USER jenkins

# Add all init groovy scripts to ref folder and change their ext to .override
# so Jenkins will override them every time it starts
COPY init-scripts/* /usr/share/jenkins/ref/init.groovy.d/

RUN cd /usr/share/jenkins/ref/init.groovy.d/ && \
    for f in *.groovy; do mv "$f" "${f}.override"; done

# Add configuration handlers groovy scripts
COPY config-handlers /usr/share/jenkins/config-handlers

VOLUME /jenkins-workspace-home

# Revert to root
USER root

COPY bin/* /usr/bin/

ENV CONFIG_FILE_LOCATION=/dev/shm/jenkins-config.yml
ENV TOKEN_FILE_LOCATION=/dev/shm/.api-token
ENV CONFIG_CACHE_DIR=/dev/shm/.jenkins-config-cache
ENV QUIET_STARTUP_FILE_LOCATION=/dev/shm/quiet-startup-mutex

####################################################################################
# GENERAL Configuration variables
####################################################################################
# Let the master be a master, don't run any jobs on it
ENV JENKINS_ENV_EXECUTERS=0
# If true, then workspaceDir will changed its defaults from ${JENKINS_HOME}/workspace
# to /jenkins-workspace-home/workspace/${ITEM_FULLNAME}
# This is useful in case your JENKINS_HOME is mapped to NFS mount,
# slowing down the workspace
ENV JENKINS_ENV_CHANGE_WORKSPACE_DIR=true
####################################################################################
# ADDITIONAL JAVA_OPTS
####################################################################################
# Each JAVA_OPTS_* variable will be added to the JAVA_OPTS variable before startup
#
# Don't run the setup wizard
ENV JAVA_OPTS_DISABLE_WIZARD="-Djenkins.install.runSetupWizard=false"
# See https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy
ENV JAVA_OPTS_CSP="-Dhudson.model.DirectoryBrowserSupport.CSP=\"sandbox allow-same-origin allow-scripts; default-src 'self'; script-src * 'unsafe-eval'; img-src *; style-src * 'unsafe-inline'; font-src *\""
# See https://issues.jenkins-ci.org/browse/JENKINS-24752
ENV JAVA_OPTS_LOAD_STATS_CLOCK="-Dhudson.model.LoadStatistics.clock=1000"
####################################################################################

####################################################################################
# JNLP Tunnel Variables
####################################################################################
# Default port for http
ENV JENKINS_HTTP_PORT_FOR_SLAVES=8080
# This is used by docker slaves to get the actual jenkins URL
# in case jenkins is behind a load-balancer or a reverse proxy
#
# JENKINS_IP_FOR_SLAVES will be evaluated in the following order:
#    $JENKINS_ENV_HOST_IP ||
#    $(eval $JENKINS_ENV_HOST_IP_CMD) ||
#    ''
#ENV JENKINS_ENV_HOST_IP=<REAL_IP>
#ENV JENKINS_ENV_HOST_IP_CMD='<command to fetch ip>'
# This variable will be evaluated and should retrun a valid IP address:
# AWS:      JENKINS_ENV_HOST_IP_CMD='curl http://169.254.169.254/latest/meta-data/local-ipv4'
# General:  JENKINS_ENV_HOST_IP_CMD='ip route | grep default | awk '"'"'{print $3}'"'"''
####################################################################################

# If sshd enabled, this will be the port
EXPOSE 16022
ENTRYPOINT ["/sbin/tini", "--", "/usr/bin/entrypoint.sh"]
