

def seedJobConfig(config){
    config.with{
        def job = jenkins.model.Jenkins.instance.getItem(name)
        def exists = (job != null)
        if(!exists){
            job = jenkins.model.Jenkins.instance.createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob, name)
        }
        def scm = new hudson.plugins.git.GitSCM(
            hudson.plugins.git.GitSCM.createRepoList(
                source?.remote, 
                source?.credentialsId
            ), 
            source?.branch ? [new hudson.plugins.git.BranchSpec("*/${source?.branch}")] : [], 
            null, 
            null, 
            null, 
            null, 
            null
        )
        job.definition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, pipeline)
        job.concurrentBuild = false
        triggers?.collect{ type, expression ->
            switch(type){
                case 'pollScm':
                    return new hudson.triggers.SCMTrigger(expression)
                case 'periodic':
                    return new hudson.triggers.TimerTrigger(expression)
                default:
                    return null
            }
        }.grep().each{ trigger -> job.addTrigger(trigger)}
        if((executeWhen == 'firstTimeOnly' && !exists) || executeWhen == 'always'){
            println "Scheduling ${name}"
            job.scheduleBuild()
        }
    }
}

def setup(config){
    config = config ?: [:]
    config.collect{k,v -> 
        seedJobConfig([name: k] << v)
    }
}

return this

