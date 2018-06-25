#!/bin/bash

SCRIPT_DIR="$BATS_TEST_DIRNAME"
TESTS_DIR="$SCRIPT_DIR/.."

TEST_CONTAINER_NAME="jenkins-test"
TEST_IMAGE_NAME="odavid/my-bloody-jenkins"
TEST_IMAGE_CONTEXT_DIR="$SCRIPT_DIR/.."

function run_test_container_and_wait(){
    docker rm -f -v "$TEST_CONTAINER_NAME" || true
    docker run --name $TEST_CONTAINER_NAME -d \
        -e JAVA_OPTS_MEM='-Xmx1g' \
        -e JENKINS_ENV_ADMIN_USER=admin \
        -v $TEST_IMAGE_CONTEXT_DIR/groovy:/tests \
        -v $TEST_IMAGE_CONTEXT_DIR/run-test.sh:/usr/bin/run-test.sh \
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

function docker_compose_up(){
    file=$1
    docker-compose -f $TESTS_DIR/$file up -d
}

function docker_compose_down(){
    file=$1
    docker-compose -f $TESTS_DIR/$file down -v --remove-orphans
}

function docker_compose_exec(){
    file=$1
    service=$2
    command="${@:3}"
    docker-compose -f $TESTS_DIR/$file exec $service $command
}


function health_check(){
    url=$1
    while ! curl -f -s $url > /dev/null
    do 
        sleep 5
    done
}