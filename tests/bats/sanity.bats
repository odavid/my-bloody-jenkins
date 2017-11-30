#!/usr/bin/env bats

load tests_helpers

@test "start container" {
    docker_build
    run_test_container_and_wait
}

@test "Sanity" {
    run_groovy_test
}

@test "ToolsConfig/General" {
    run_groovy_test
}

@test "ToolsConfig/Maven" {
    run_groovy_test
}

@test "ToolsConfig/Ant" {
    run_groovy_test
}

@test "ToolsConfig/JDK" {
    run_groovy_test
}

@test "ToolsConfig/Gradle" {
    run_groovy_test
}

@test "ToolsConfig/Xvfb" {
    run_groovy_test
}

@test "ToolsConfig/SonarQubeRunner" {
    run_groovy_test
}

@test "ToolsConfig/CustomInstallers" {
    run_groovy_test
}

@test "terminate container" {
    teardown_test_container
}
