#!/usr/bin/env bats

load tests_helpers

@test "start container" {
    run_test_container_and_wait
}

@test "Sanity" {
    run_groovy_test
}

@test "RemoveMasterEnvVarsConfig" {
    run_groovy_test
}

@test "CloudsConfig" {
    run_groovy_test
}

@test "CredsConfig" {
    run_groovy_test
}

@test "ToolsConfig" {
    run_groovy_test
}

@test "SecurityConfig" {
    run_groovy_test
}

@test "CheckmarxConfig" {
    run_groovy_test
}

@test "EnvironmentVarsConfig" {
    run_groovy_test
}

@test "GitlabConfig" {
    run_groovy_test
}

@test "JiraConfig" {
    run_groovy_test
}

@test "NotifiersConfig" {
    run_groovy_test
}

@test "PipelineLibrariesConfig" {
    run_groovy_test
}

@test "ScriptApprovalConfig" {
    run_groovy_test
}

@test "SeedJobsConfig" {
    run_groovy_test
}

@test "JobDSLScriptsConfig" {
    run_groovy_test
}

@test "SonarQubeServersConfig" {
    run_groovy_test
}

@test "terminate container" {
    teardown_test_container
}
