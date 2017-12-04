#!/bin/bash -e

script_dir=$(cd $(dirname "$0"); pwd)
token_file="$TOKEN_FILE_LOCATION"
AUTH_ARG=""

if [ -f $token_file ]; then
    AUTH_ARG="-auth $(cat $token_file)"
fi

if [ ! -f $script_dir/jenkins-cli.jar ]; then
    curl -o $script_dir/jenkins-cli.jar http://localhost:8080/jnlpJars/jenkins-cli.jar
fi

echo "Running test $1"
java -jar $script_dir/jenkins-cli.jar \
    -s http://localhost:8080/ $AUTH_ARG \
    groovy = < "$1"
echo "Running test $1... done"
