#! /bin/bash -e

if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then
    # Don't run the setup wizard
    export JAVA_OPTS="$JAVA_OPTS -Djenkins.install.runSetupWizard=false"
    # See https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy
    export JAVA_OPTS="$JAVA_OPTS -Dhudson.model.DirectoryBrowserSupport.CSP=\"sandbox allow-same-origin allow-scripts; default-src 'self'; script-src * 'unsafe-eval'; img-src *; style-src * 'unsafe-inline'; font-src *\""

    # We convert all JENKINS_ENV_* variables to be located in a properties file
    # and then unset them. We don't want them on the jenkins config page!
    rm -rf /tmp/jenkins-env.properties
    VARIABLES=$(compgen -v | while read line; do echo $line | grep JENKINS_ENV;done) || true
    for key in $VARIABLES; do
        echo "reading key: ${key}"
        value="${!key}"
        value="$(echo -n "$value" | awk 1 ORS='\\n' | sed 's/\([=:]\)/\\\1/g')"
        trimmed_key=$(echo -n $key | sed 's/JENKINS_ENV_//g')
        echo "${trimmed_key}=$value" >> /tmp/jenkins-env.properties
        unset ${key}
    done

    # This is important if you let docker create the host mounted volumes. 
    # We need to make sure they will be owned by the jenkins user
    chown -R jenkins:jenkins /jenkins-workspace-home
    chown -R jenkins:jenkins $JENKINS_HOME
    # This changes the actual command to run the original jenkins entrypoint
    # using the jenkins user
    set -- gosu jenkins /usr/local/bin/jenkins-orig.sh "$@"
fi

exec "$@"

