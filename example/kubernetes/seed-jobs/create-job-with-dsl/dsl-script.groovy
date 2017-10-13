pipelineJob('sample-job-created-by-seed'){
    definition{
        cpsScm{
            git('https://github.com/odavid/my-bloody-jenkins.git', 'kubernetes-example')
            scriptPath('example/kubernetes/jobs/sample-job/Jenkinsfile')
        }
    }
}