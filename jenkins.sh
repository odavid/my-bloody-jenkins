#! /bin/bash -e

if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then
    JAVA_OPTS_VARIABLES=$(compgen -v | while read line; do echo $line | grep JAVA_OPTS_;done) || true
    for key in $JAVA_OPTS_VARIABLES; do
        echo "adding: ${key} to JAVA_OPTS"
        export JAVA_OPTS="$JAVA_OPTS ${!key}"
    done

    if [ -n "${JENKINS_ENV_CONFIG_YAML}" ]; then
        echo -n "$JENKINS_ENV_CONFIG_YAML" > /etc/jenkins-config.yml
        unset JENKINS_ENV_CONFIG_YAML
    elif [ -n "${JENKINS_ENV_CONFIG_YML_S3_URL}" ]; then
        echo "Fetching config from S3: ${JENKINS_ENV_CONFIG_YML_S3_URL}"
        watch-s3-file.sh \
             --s3-url "${JENKINS_ENV_CONFIG_YML_S3_URL}" \
            --filename /etc/jenkins-config.yml \
            --skip-watch
        if [ "$JENKINS_ENV_CONFIG_YML_S3_DISABLE_WATCH" != 'true' ]; then
            echo "Watching config from S3: ${JENKINS_ENV_CONFIG_YML_S3_URL} in the backgroud"
            watch-s3-file.sh \
                --s3-url "${JENKINS_ENV_CONFIG_YML_S3_URL}" \
                --filename /etc/jenkins-config.yml \
                --polling-interval "${JENKINS_ENV_CONFIG_YML_S3_POLLING:-30}" \
                --command 'update-config.sh' &
        fi
        unset AWS_ACCESS_KEY_ID
        unset AWS_SECRET_ACCESS_KEY
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
    if [ "jenkins" != "$(stat -c %U /jenkins-workspace-home/workspace)" ]; then
        chown -R jenkins:jenkins /jenkins-workspace-home
    fi
    if [ "jenkins" != "$(stat -c %U ${JENKINS_HOME})" ]; then
        chown -R jenkins:jenkins $JENKINS_HOME
    fi

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