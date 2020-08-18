def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def getOrCreateFolder(def parent, def name) {
    def folder
    if (name == "") {
        return null
    }
    if (parent == null) {
        parent = jenkins.model.Jenkins.instance
        folder = jenkins.model.Jenkins.instance.getItem(name)
    } else {
        folder = jenkins.model.Jenkins.instance.getItemByFullName("${parent.getFullName()}/${name}")
    }

    if (folder != null) {
        if (folder instanceof com.cloudbees.hudson.plugins.folder.Folder) {
            return folder
        } else {
            throw new Exception("${folder} already exists, but it's not a folder")
        }
    }
    return parent.createProject(com.cloudbees.hudson.plugins.folder.Folder, name)
}


def seedJobConfig(config){
    config.with{
        def items = name.split("/")
        def jobName = items[-1] //Get the job name from the path
        def folders = items.dropRight(1)
        def currentFolder = null
        folders.each {
            currentFolder = getOrCreateFolder(currentFolder, it)
        }
        def job
        if (currentFolder) {
            job = currentFolder.getItem(jobName)
        } else {
            job = jenkins.model.Jenkins.instance.getItem(jobName)
        }
        def exists = (job != null)
        if(!exists){
            if (currentFolder) {
                job = currentFolder.createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob, jobName)
            } else {
                job = jenkins.model.Jenkins.instance.createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob, jobName)
            }
        }
        def scm = new hudson.plugins.git.GitSCM(
            hudson.plugins.git.GitSCM.createRepoList(
                source?.remote,
                source?.credentialsId
            ),
            source?.branch ? [new hudson.plugins.git.BranchSpec("${source?.branch}")] : [],
            null,
            null,
            null,
            null,
            null
        )
        job.definition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, pipeline)
        job.concurrentBuild = asBoolean(concurrentBuild)
        triggers?.collect{ type, expression ->
            switch(type){
                case 'pollScm':
                    return new hudson.triggers.SCMTrigger(expression)
                case 'periodic':
                    return new hudson.triggers.TimerTrigger(expression)
                case 'artifactory':
                    trigger = new org.jfrog.hudson.trigger.ArtifactoryTrigger(expression.path, expression.schedule)
                    trigger.details = new org.jfrog.hudson.ServerDetails(expression.serverId, null, null, null, null, null)
                    return trigger
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

