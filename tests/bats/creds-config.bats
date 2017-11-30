#!/usr/bin/env bats

load tests_helpers

@test "start container" {
    docker_build
    run_test_container_and_wait
}

@test "CredsConfig/General" {
    run_groovy_test
}

@test "terminate container" {
    teardown_test_container
}
