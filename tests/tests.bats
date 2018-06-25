#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_unit_test(){
    run_groovy_script $COMPOSE_FILE groovy/${BATS_TEST_DESCRIPTION}Test.groovy
}

@test "start jenkins" {
    create_docker_network
    docker_compose_up $COMPOSE_FILE
    health_check http://0.0.0.0:8080/login
}

@test "Sanity" {
    groovy_unit_test
}

@test "RemoveMasterEnvVarsConfig" {
    groovy_unit_test
}

@test "CloudsConfig" {
    groovy_unit_test
}

@test "CredsConfig" {
    groovy_unit_test
}

@test "ToolsConfig" {
    groovy_unit_test
}

@test "SecurityConfig" {
    groovy_unit_test
}

@test "CheckmarxConfig" {
    groovy_unit_test
}

@test "EnvironmentVarsConfig" {
    groovy_unit_test
}

@test "GitlabConfig" {
    groovy_unit_test
}

@test "JiraConfig" {
    groovy_unit_test
}

@test "NotifiersConfig" {
    groovy_unit_test
}

@test "PipelineLibrariesConfig" {
    groovy_unit_test
}

@test "ScriptApprovalConfig" {
    groovy_unit_test
}

@test "SeedJobsConfig" {
    groovy_unit_test
}

@test "JobDSLScriptsConfig" {
    groovy_unit_test
}

@test "SonarQubeServersConfig" {
    groovy_unit_test
}

@test "terminate jenkins" {
    docker_compose_down $COMPOSE_FILE 
    destroy_docker_network
}
