#!/bin/bash

export TESTS_DIR="$BATS_TEST_DIRNAME"
export TESTS_HOST_CONF_DIR="$TESTS_DIR/confdir"
export TESTS_CONTAINER_TESTS_DIR=/tests
export TESTS_CONTAINER_CONF_DIR=/confdir

function run_groovy_script(){
    file=$1
    script=$2
    service=jenkins
    docker_compose_exec $file $service $TESTS_CONTAINER_TESTS_DIR/run-jenkins-groovy.sh $TESTS_CONTAINER_TESTS_DIR/$script
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