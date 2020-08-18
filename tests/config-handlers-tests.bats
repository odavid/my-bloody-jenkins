#!/usr/bin/env bats

load tests_helpers

COMPOSE_FILE=docker-compose-simple.yml

function groovy_test(){
    run_groovy_script $COMPOSE_FILE groovy/config-handlers/${BATS_TEST_DESCRIPTION}Test.groovy
}

@test ">>> setup config-handlers tests env" {
    touch_config
    create_docker_network
    docker_compose_up $COMPOSE_FILE
    health_check http://0.0.0.0:8080/login
}

# @test "Sanity" {
#     groovy_test
# }

# @test "ConfigurationAsCodeConfig" {
#     groovy_test
# }

# @test "RemoveMasterEnvVarsConfig" {
#     groovy_test
# }

@test "ArtifactoryConfig" {
    groovy_test
}

# @test "CloudsConfig" {
#     groovy_test
# }

# @test "CredsConfig" {
#     groovy_test
# }

# @test "ToolsConfig" {
#     groovy_test
# }

# @test "SecurityConfig" {
#     groovy_test
# }

# @test "CheckmarxConfig" {
#     groovy_test
# }

# @test "EnvironmentVarsConfig" {
#     groovy_test
# }

# @test "GitlabConfig" {
#     groovy_test
# }

# @test "JiraConfig" {
#     groovy_test
# }

# @test "JiraStepsConfig" {
#     groovy_test
# }

# @test "NotifiersConfig" {
#     groovy_test
# }

# @test "PipelineLibrariesConfig" {
#     groovy_test
# }

# @test "ScriptApprovalConfig" {
#     groovy_test
# }

# @test "SeedJobsConfig" {
#     groovy_test
# }

# @test "JobDSLScriptsConfig" {
#     groovy_test
# }

# @test "SonarQubeServersConfig" {
#     groovy_test
# }

@test "<<< teardown config-handlers tests env" {
    docker_compose_down $COMPOSE_FILE
    destroy_docker_network
    rm -rf $TESTS_HOST_CONF_DIR
}
