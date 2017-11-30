#!/usr/bin/env bats

load tests_helpers

@test "start container" {
    docker_build
    run_test_container_and_wait
}

@test "SanityTest" {
    run_test "SanityTest"
}

@test "ToolsConfig General" {
    run_test "ToolsConfig/GeneralTest"
}

@test "ToolsConfig Maven" {
    run_test "ToolsConfig/MavenTest"
}

@test "ToolsConfig Ant" {
    run_test "ToolsConfig/AntTest"
}

@test "terminate container" {
    teardown_test_container
}
