FROM jenkins/jenkins:2.60.3

ARG GOSU_VERSION=1.10

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/ref/plugins.txt | tr '\n' ' ')

# We will disable some sandbox limitations
COPY sandbox-signatures.txt /usr/share/jenkins/ref/sandbox-signatures.txt.override
COPY init-scripts/* /usr/share/jenkins/ref/init.groovy.d/

# Using root to install and run entrypoint. 
# We will change the user to jenkins using gosu
USER root
RUN curl -SsLo /usr/bin/gosu https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-amd64 && \
     chmod +x /usr/bin/gosu

# Separate between JENKINS_HOME and WORKSPACE dir. Best if we use NFS for JENKINS_HOME 
RUN mkdir -p /jenkins-workspace-home && \
    chown -R jenkins:jenkins /jenkins-workspace-home
VOLUME /jenkins-workspace-home

# Change the original entrypoint. We will later on run it using gosu
RUN mv /usr/local/bin/jenkins.sh /usr/local/bin/jenkins-orig.sh
COPY jenkins.sh /usr/local/bin/jenkins.sh

ENV \
    JENKINS_ENV_CLI_OVER_REMOTING=false \
    JENKINS_ENV_EXECUTERS_COUNT=0
