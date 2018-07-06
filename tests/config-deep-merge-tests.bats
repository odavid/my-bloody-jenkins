#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_test(){
    run_groovy_script $COMPOSE_FILE groovy/deep-merge/$1
}

@test ">>> setup config deep-merge with multiple data sources env" {
    mkdir -p $TESTS_HOST_CONF_DIR/{dir1,dir2,dir3}
    create_docker_network

    JENKINS_ENV_CONFIG_YML_URL="file://${TESTS_CONTAINER_CONF_DIR}/dir1,file://${TESTS_CONTAINER_CONF_DIR}/dir2,file://${TESTS_CONTAINER_CONF_DIR}/dir3/*.yml" \
    docker_compose_up $COMPOSE_FILE

    health_check http://0.0.0.0:8080/login
}

@test "test values comming from dir1" {
    cp $TESTS_DIR/data/config-fixtures/config-in-dir1.yml $TESTS_HOST_CONF_DIR/dir1
    sleep 15
    groovy_test AssertCredsFromDir1.groovy
}

@test "test values comming from dir1 and dir2" {
    cp $TESTS_DIR/data/config-fixtures/config-in-dir2.yml $TESTS_HOST_CONF_DIR/dir2
    sleep 15
    groovy_test AssertCredsFromDir2.groovy
}

@test "test values comming from dir1 and dir2 and dir3-1 (glob expression)" {
    cp $TESTS_DIR/data/config-fixtures/config-in-dir3-1.yml $TESTS_HOST_CONF_DIR/dir3
    sleep 15
    groovy_test AssertCredsFromDir31.groovy
}

@test "test values comming from dir1 and dir2 and dir3-2 (glob expression)" {
    cp $TESTS_DIR/data/config-fixtures/config-in-dir3-2.yml $TESTS_HOST_CONF_DIR/dir3
    sleep 15
    groovy_test AssertCredsFromDir32.groovy
}

@test "<<< teardown config deep-merge with multiple data sources env" {
    docker_compose_down $COMPOSE_FILE
    rm -rf $TESTS_HOST_CONF_DIR
    destroy_docker_network
}