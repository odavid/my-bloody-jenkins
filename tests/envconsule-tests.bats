#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_unit_test(){
    run_groovy_script $COMPOSE_FILE groovy/${BATS_TEST_DESCRIPTION}Test.groovy
}

@test "setup envconsul tests env" {
    touch_config
    create_docker_network
    docker_compose_up docker-compose-consul.yml
    health_check http://0.0.0.0:8500/v1/status/leader
    docker_compose_exec docker-compose-consul.yml consul consul kv import @${TESTS_CONTAINER_TESTS_DIR}/data/consul-data.json
    CONSUL_ADDR="consul:8500" ENVCONSUL_CONSUL_PREFIX=jenkins docker_compose_up $COMPOSE_FILE
    health_check http://0.0.0.0:8080/login
}

@test "test values comming from consul" {
    config_from_fixture $TESTS_DIR/data/config-fixtures/creds-from-consul.yml
    sleep 10
    run_groovy_script $COMPOSE_FILE groovy/AssertCredsFromConsul.groovy
}

@test "teardown envconsul tests env" {
    docker_compose_down docker-compose-consul.yml
    docker_compose_down $COMPOSE_FILE
    rm -rf $TESTS_HOST_CONF_DIR
    destroy_docker_network
}