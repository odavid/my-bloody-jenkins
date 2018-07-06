#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_test(){
    run_groovy_script $COMPOSE_FILE groovy/envvars/$1
}

@test ">>> setup config-envvars env" {
    mkdir -p $TESTS_HOST_CONF_DIR/{conf,secret1,secret2}
    echo -n "username-secret1" > $TESTS_HOST_CONF_DIR/secret1/username
    echo -n "password-secret1" > $TESTS_HOST_CONF_DIR/secret1/password
    echo -n "username-secret2" > $TESTS_HOST_CONF_DIR/secret2/username
    echo -n "password-secret2" > $TESTS_HOST_CONF_DIR/secret2/password
    create_docker_network

    JENKINS_ENV_CONFIG_YML_URL="file://${TESTS_CONTAINER_CONF_DIR}/conf" \
    ENVVARS_DIRS=${TESTS_CONTAINER_CONF_DIR}/secret1,${TESTS_CONTAINER_CONF_DIR}/secret2 \
    docker_compose_up $COMPOSE_FILE

    health_check http://0.0.0.0:8080/login
}

@test "test values comming from secret1" {
    cp $TESTS_DIR/data/config-fixtures/config-envvars-secret1.yml $TESTS_HOST_CONF_DIR/conf/config.yml
    sleep 15
    groovy_test AssertCredsFromSecret1.groovy
}

@test "test values comming from secret2" {
    cp $TESTS_DIR/data/config-fixtures/config-envvars-secret2.yml $TESTS_HOST_CONF_DIR/conf/config.yml
    sleep 15
    groovy_test AssertCredsFromSecret2.groovy
}

@test "test values changed " {
    echo -n "username-secret1" > $TESTS_HOST_CONF_DIR/secret2/username
    echo -n "password-secret1" > $TESTS_HOST_CONF_DIR/secret2/password
    sleep 15
    groovy_test AssertCredsFromSecret1.groovy
}

@test "<<< teardown config-envvars env" {
    docker_compose_down $COMPOSE_FILE
    rm -rf $TESTS_HOST_CONF_DIR
    destroy_docker_network
}