#!/usr/bin/env bats

load docker_compose_helpers

export TESTS_CONF_DIR=$BATS_TEST_DIRNAME/../confdir

@test "initialize" {
    mkdir -p $TESTS_CONF_DIR
    touch $TESTS_CONF_DIR/config.yml
}

@test "start consul" {
    docker_compose_up docker-compose-consul.yml
    health_check http://0.0.0.0:8500/v1/status/leader
}

@test "import consul data" {
    docker_compose_exec docker-compose-consul.yml consul consul kv import @/data/data/consul-data.json
}

@test "start jenkins" {
    health_check http://0.0.0.0:8080/login
}

@test "write creds" {

cat << EOF > $TESTS_CONF_DIR/config.yml
    credentials:
        git-user-pass:
            type: userpass
            username: \${GIT_USERNAME}
            password: \${GIT_PASSWORD}

EOF
    
    sleep 10

}

# @test "terminate consul" {
#     docker_compose_down docker-compose-consul.yml
# }

# @test "terminate jenkins" {
#     docker_compose_down docker-compose-simple.yml
# }

# @test "cleanup" {
#     rm -rf $TESTS_CONF_DIR
# }