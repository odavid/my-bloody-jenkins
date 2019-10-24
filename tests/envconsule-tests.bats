#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_test(){
    run_groovy_script $COMPOSE_FILE groovy/envconsul/$1
}

@test ">>> setup envconsul tests env" {
    touch_config
    create_docker_network
    docker_compose_up docker-compose-consul.yml
    health_check http://0.0.0.0:8500/v1/status/leader
    health_check http://0.0.0.0:8200/v1/sys/health

    docker_compose_exec docker-compose-consul.yml consul consul kv put jenkins/git_password password
    docker_compose_exec docker-compose-consul.yml consul consul kv put jenkins/git_username username
    docker_compose_exec docker-compose-consul.yml vault vault kv put secret/jenkins top_secret=very_SECRET

    CONSUL_ADDR="consul:8500" \
    VAULT_TOKEN="vault-root-token" \
    VAULT_ADDR="http://vault:8200" \
    ENVCONSUL_CONSUL_PREFIX=jenkins \
    ENVCONSUL_VAULT_PREFIX="secret/jenkins" \
    ENVCONSUL_ADDITIONAL_ARGS="-vault-renew-token=false" \
    JENKINS_ENV_CONFIG_YML_URL=file://${TESTS_CONTAINER_CONF_DIR}/config.yml \
    docker_compose_up $COMPOSE_FILE

    health_check http://0.0.0.0:8080/login
}

@test "test values comming from consul" {
    config_from_fixture $TESTS_DIR/data/config-fixtures/creds-from-consul.yml
    sleep $SLEEP_TIME_BEFORE_CHECKS
    groovy_test AssertCredsFromConsul.groovy
}

@test "test values comming from vault" {
    config_from_fixture $TESTS_DIR/data/config-fixtures/creds-from-vault.yml
    sleep $SLEEP_TIME_BEFORE_CHECKS
    groovy_test AssertCredsFromVault.groovy
}

@test "<<< teardown envconsul tests env" {
    docker_compose_down docker-compose-consul.yml
    docker_compose_down $COMPOSE_FILE
    rm -rf $TESTS_HOST_CONF_DIR
    destroy_docker_network
}