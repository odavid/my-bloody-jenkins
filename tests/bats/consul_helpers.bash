#!/bin/bash

SCRIPT_DIR="$BATS_TEST_DIRNAME"

function run_consul(){
    docker-compose -f docker-compose-consul.yml up -d
    while ! curl -f localhost:8500/v1/status/leader
    do 
        sleep 5
    done
}

function import_consul_data(){
    data_file=$1
    docker-compose -f docker-compose-consul.yml exec consul consul kv import @/data/data/consul-data.json
}

function terminate_consul(){
    docker-compose -f docker-compose-consul.yml down -v --remove-orphans
}

