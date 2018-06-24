#!/bin/bash

SCRIPT_DIR="$BATS_TEST_DIRNAME"
TESTS_DIR="$SCRIPT_DIR/.."
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