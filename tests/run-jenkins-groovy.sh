#!/bin/bash -e

script_dir=$(cd $(dirname "$0"); pwd)
token_file="$TOKEN_FILE_LOCATION"
AUTH_ARG=""

if [ -f $token_file ]; then
    AUTH_ARG="-auth $(cat $token_file)"
fi

if [ ! -f /tmp/jenkins-cli.jar ]; then
    curl -o /tmp/jenkins-cli.jar http://localhost:8080/jnlpJars/jenkins-cli.jar
fi

echo "Running test $1"
java -jar /tmp/jenkins-cli.jar \
    -s http://localhost:8080/ $AUTH_ARG \
    groovy = < "$1"
echo "Running test $1... done"
