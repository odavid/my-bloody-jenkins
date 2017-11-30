#!/usr/bin/env bats

load tests_helpers

@test "start container" {
    docker_build
    run_test_container_and_wait
    teardown_test_container
}