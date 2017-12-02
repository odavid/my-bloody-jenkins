def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}


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
        if(parameters){
            def paramsDef = job.getProperty(hudson.model.ParametersDefinitionProperty)
            if(!paramsDef){
                paramsDef = new hudson.model.ParametersDefinitionProperty([])
                job.addProperty(paramsDef)
            }
            paramsDef.parameterDefinitions.clear()
            paramsDef.parameterDefinitions.addAll(
                parameters?.collect{ name, parameter ->
                    if(parameter instanceof String){
                        def type = parameter.contains('\n') ? 'text' : 'string'
                        parameter = [type: type, value: parameter]
                    }else if(parameter instanceof List){
                        parameter = [type: 'choice', choices: parameter]
                    }else if(parameter instanceof Boolean){
                        parameter = [type: 'boolean', value: parameter]
                    }
                    parameter.with{
                        switch(type){
                            case 'choice':
                                new hudson.model.ChoiceParameterDefinition(
                                    name, 
                                    choices?.join('\n')?:'', 
                                    description?:''
                                )
                                break
                            case 'boolean':
                                new hudson.model.BooleanParameterDefinition(
                                    name,
                                    asBoolean(value),
                                    description?:''
                                )
                                break
                            case 'password':
                                new hudson.model.PasswordParameterValue(
                                    name,
                                    value ? value.toString() : '',
                                    description?:''
                                )
                                break
                            case 'text':
                                new hudson.model.TextParameterDefinition(
                                    name,
                                    value ? value.toString() : '',
                                    description?:''
                                )
                                break
                            case 'string':
                            default:
                                new hudson.model.StringParameterDefinition(
                                    name,
                                    value ? value.toString() : '',
                                    description?:''
                                )
                                break
                        }
                    }
                } ?: []
            )
        }
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

