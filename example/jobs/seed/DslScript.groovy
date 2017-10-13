pipelineJob('sample-job-created-by-seed'){
    definition{
        cpsScm{
            scm{
                git('https://github.com/odavid/my-bloody-jenkins.git', 'kubernetes-example')
            }
            scriptPath('example/jobs/sample-job/Jenkinsfile')
        }
    }
}