FROM jenkins/jenkins:2.73.1-alpine

ARG GOSU_VERSION=1.10

# Using root to install and run entrypoint. 
# We will change the user to jenkins using gosu
USER root

# Ability to use usermod
RUN apk add --no-cache shadow

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Add all init groovy scripts to ref folder and change their ext to .override
# so Jenkins will override them every time it starts
COPY init-scripts/* /usr/share/jenkins/ref/init.groovy.d/
RUN cd /usr/share/jenkins/ref/init.groovy.d/ && \
    for f in *.groovy; do mv "$f" "${f}.override"; done 

# Add configuration handlers groovy scripts
COPY config-handlers /usr/share/jenkins/config-handlers
COPY update-config.sh /usr/bin/

RUN curl -SsLo /usr/bin/gosu https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-amd64 && \
     chmod +x /usr/bin/gosu


# Separate between JENKINS_HOME and WORKSPACE dir. Best if we use NFS for JENKINS_HOME 
RUN mkdir -p /jenkins-workspace-home && \
    chown -R jenkins:jenkins /jenkins-workspace-home

VOLUME /jenkins-workspace-home

# Change the original entrypoint. We will later on run it using gosu
RUN mv /usr/local/bin/jenkins.sh /usr/local/bin/jenkins-orig.sh
COPY jenkins.sh /usr/local/bin/jenkins.sh

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
# This is used by docker slaves to construct the '-tunnel' parameter 
# in case jenkins is behind a load-balancer or a reverse proxy
# i.e. (-tunnel $JENKINS_IP_FOR_SLAVES)
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

