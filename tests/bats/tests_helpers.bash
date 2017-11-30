#!/bin/bash

SCRIPT_DIR="$BATS_TEST_DIRNAME"

TEST_CONTAINER_NAME="jenkins-test"
TEST_IMAGE_NAME="odavid/my-bloody-jenkins-tests"
TEST_IMAGE_CONTEXT_DIR="$SCRIPT_DIR/.."

function docker_build(){
    docker rm -f -v "$TEST_CONTAINER_NAME" || true
    docker build --rm --force-rm --no-cache -t "$TEST_IMAGE_NAME" "${TEST_IMAGE_CONTEXT_DIR}"
}

function run_test_container_and_wait(){
    docker run --name $TEST_CONTAINER_NAME -d \
        -e JAVA_OPTS_MEM='-Xmx1g' \
        -e JENKINS_ENV_ADMIN_USER=admin \
        -e JENKINS_ENV_CONFIG_YAML="
    security:
        realm: jenkins_database
        adminPassword: admin
    " \
        $TEST_IMAGE_NAME

    JENKINS_IS_UP=1
    echo "before waiting JENKINS_IS_UP: $JENKINS_IS_UP"
    while [ "$JENKINS_IS_UP" != "0" ]; do
        JENKINS_IS_UP=$(docker logs jenkins-test 2>&1 | grep "Jenkins is fully up and running" > /dev/null; echo $?)
        echo "Waiting for jenkins to be up... JENKINS_IS_UP=$JENKINS_IS_UP"
        sleep 2
    done
}

function teardown_test_container(){
    docker rm -f -v "$TEST_CONTAINER_NAME" || true
}

function run_groovy_test(){
    local test_name=${1:-$BATS_TEST_DESCRIPTION}
    docker exec -t $TEST_CONTAINER_NAME run-test.sh /tests/${test_name}Test.groovy
}

