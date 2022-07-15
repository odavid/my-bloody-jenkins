#! /bin/bash -e

if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then
    JAVA_OPTS_VARIABLES=$(compgen -v | while read line; do echo $line | grep JAVA_OPTS_;done) || true
    for key in $JAVA_OPTS_VARIABLES; do
        echo "adding: ${key} to JAVA_OPTS"
        export JAVA_OPTS="$JAVA_OPTS ${!key}"
    done

    if [ -n "${JENKINS_ENV_CONFIG_YAML}" ]; then
        echo -n "$JENKINS_ENV_CONFIG_YAML" > $CONFIG_FILE_LOCATION
        unset JENKINS_ENV_CONFIG_YAML
    elif [ -n "${JENKINS_ENV_CONFIG_YML_URL}" ]; then
        echo "Fetching config from URL: ${JENKINS_ENV_CONFIG_YML_URL}"
        watch-config.sh \
             --debug \
             --cache-dir $CONFIG_CACHE_DIR \
             --url "${JENKINS_ENV_CONFIG_YML_URL}" \
            --skip-watch

        if [ "$JENKINS_ENV_CONFIG_YML_URL_DISABLE_WATCH" != 'true' ]; then
            echo "Watching config from URL: ${JENKINS_ENV_CONFIG_YML_URL} in the backgroud"
            nohup watch-config.sh \
                --cache-dir $CONFIG_CACHE_DIR \
                --url "${JENKINS_ENV_CONFIG_YML_URL}" \
                --polling-interval "${JENKINS_ENV_CONFIG_YML_URL_POLLING:-30}" &
        fi
        unset AWS_ACCESS_KEY_ID
        unset AWS_SECRET_ACCESS_KEY
    fi

    if [ -n "$JENKINS_ENV_PLUGINS" ]; then
        echo "Installing additional plugins $JENKINS_ENV_PLUGINS"
        jenkins-plugin-cli --plugins $(echo $JENKINS_ENV_PLUGINS | tr ',' ' ')
        chown jenkins:jenkins /usr/share/jenkins/ref/
        echo "Installing additional plugins. Done..."
    fi

    # Because we are in docker, we need to fetch the real IP of jenkins, so ecs/kubernetes/docker cloud slaves will
    # be able to connect to it
    # If it is running with docker network=host, then the default ip address will be sufficient
    if [ -n "${JENKINS_ENV_HOST_IP}" ]; then
        export JENKINS_IP_FOR_SLAVES="${JENKINS_ENV_HOST_IP}"
        unset JENKINS_ENV_HOST_IP
    elif [ -n "${JENKINS_ENV_HOST_IP_CMD}" ]; then
        export JENKINS_IP_FOR_SLAVES="$(eval ${JENKINS_ENV_HOST_IP_CMD})" || true
        unset JENKINS_ENV_HOST_IP_CMD
    fi
    echo "JENKINS_IP_FOR_SLAVES = ${JENKINS_IP_FOR_SLAVES}"


    # This is important if you let docker create the host mounted volumes.
    # We need to make sure they will be owned by the jenkins user
    mkdir -p /jenkins-workspace-home/workspace
    echo "Chowning /jenkins-workspace-home"
    if [ "jenkins" != "$(stat -c %U /jenkins-workspace-home/workspace)" ]; then
        chown -R jenkins:jenkins /jenkins-workspace-home
    fi
    echo "Chowning /jenkins-workspace-home. Done"
    if [ ! -n "${DISABLE_CHOWN_ON_STARTUP}" ]; then
        echo "Chowning $JENKINS_HOME"
        if [ "jenkins" != "$(stat -c %U ${JENKINS_HOME})" ]; then
            chown -R jenkins:jenkins $JENKINS_HOME
        fi
        echo "Chowning $JENKINS_HOME. Done"
        unset DISABLE_CHOWN_ON_STARTUP
    else
        echo "Chowning $JENKINS_HOME disabled"
    fi

    # To enable docker cloud based on docker socket,
    # we need to add jenkins user to the docker group
    if [ -S /var/run/docker.sock ]; then
        DOCKER_SOCKET_OWNER_GROUP_ID=$(stat -c %g /var/run/docker.sock)
        groupadd -for -g ${DOCKER_SOCKET_OWNER_GROUP_ID} docker
        id jenkins -G -n | grep docker || usermod -aG docker jenkins
    fi

    # This changes the actual command to run the original jenkins entrypoint
    # using the jenkins user
    set -- gosu jenkins /usr/local/bin/jenkins.sh "$@"
fi

exec "$@"