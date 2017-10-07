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

# We will disable some sandbox limitations
COPY sandbox-signatures.txt /usr/share/jenkins/ref/sandbox-signatures.txt.override

RUN curl -SsLo /usr/bin/gosu https://github.com/tianon/gosu/releases/download/${GOSU_VERSION}/gosu-amd64 && \
     chmod +x /usr/bin/gosu


# Separate between JENKINS_HOME and WORKSPACE dir. Best if we use NFS for JENKINS_HOME 
RUN mkdir -p /jenkins-workspace-home && \
    chown -R jenkins:jenkins /jenkins-workspace-home
VOLUME /jenkins-workspace-home

# Change the original entrypoint. We will later on run it using gosu
RUN mv /usr/local/bin/jenkins.sh /usr/local/bin/jenkins-orig.sh
COPY jenkins.sh /usr/local/bin/jenkins.sh
