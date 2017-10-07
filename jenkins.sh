#! /bin/bash -e

if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then
    # Don't run the setup wizard
    export JAVA_OPTS="$JAVA_OPTS -Djenkins.install.runSetupWizard=false"
    # See https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy
    export JAVA_OPTS="$JAVA_OPTS -Dhudson.model.DirectoryBrowserSupport.CSP=\"sandbox allow-same-origin allow-scripts; default-src 'self'; script-src * 'unsafe-eval'; img-src *; style-src * 'unsafe-inline'; font-src *\""

    if [ -n "${JENKINS_ENV_CONFIG_YAML}" ]; then
        echo -n "$JENKINS_ENV_CONFIG_YAML" > /etc/jenkins-config.yml
        unset JENKINS_ENV_CONFIG_YAML
    fi
    
    # This is important if you let docker create the host mounted volumes. 
    # We need to make sure they will be owned by the jenkins user
    chown -R jenkins:jenkins /jenkins-workspace-home
    chown -R jenkins:jenkins $JENKINS_HOME

    # To enable docker cloud based on docker socket, 
    # we need to add jenkins user to the docker group
    if [ -S /var/run/docker.sock ]; then
        DOCKER_SOCKET_OWNER_GROUP_ID=$(stat -c %g /var/run/docker.sock)
        groups jenkins | grep docker || groupadd -g $DOCKER_SOCKET_OWNER_GROUP_ID docker
        id jenkins -G | grep $DOCKER_SOCKET_OWNER_GROUP_ID || usermod -G docker jenkins
    fi

    # This changes the actual command to run the original jenkins entrypoint
    # using the jenkins user
    set -- gosu jenkins /usr/local/bin/jenkins-orig.sh "$@"
fi

exec "$@"

