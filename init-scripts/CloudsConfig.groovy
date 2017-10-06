import hudson.slaves.JNLPLauncher
import hudson.plugins.sshslaves.SSHConnector
import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.DockerImagePullStrategy
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerJNLPLauncher
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerSSHLauncher
import com.nirima.jenkins.plugins.docker.strategy.DockerOnceRetentionStrategy
import hudson.model.Node
import jenkins.model.Jenkins


def dockerCloud(config){
    config.with{
        return new DockerCloud(
            id,
            templates?.collect{ temp ->
                def dockerComputerJNLPLauncher = new DockerComputerJNLPLauncher(
                    new JNLPLauncher(
                        temp.tunnel,
                        temp.vmargs
                    )
                )
                dockerComputerJNLPLauncher.setUser(temp.user)
                def dockerTemplate = new DockerTemplate(
                    new DockerTemplateBase(
                        temp.image,
                        temp.dns?.join(' '),
                        temp.network,
                        temp.command,
                        temp.volumes?.join(' '),
                        temp.volumesFrom?.join(' '),
                        temp.environment?.collect{k,v -> "${k}=${v}"}.join("\n"),
                        null, // temp.lxcConfString,
                        temp.hostname,
                        temp.memory?.toInteger(),
                        temp.memorySwap?.toInteger(),
                        temp.cpu?.toInteger(),
                        temp.ports?.join(' '),
                        temp.bindAllPorts?.toBoolean() ?: false,
                        temp.privileged?.toBoolean() ?: false,
                        temp.tty?.toBoolean() ?: false,
                        temp.macAddress
                    ),
                    temp.labels?.join(' '),
                    temp.remoteFs,
                    temp.remoteFsMapping,
                    "" //temp.instanceCapStr,
                )
                dockerTemplate.mode = Node.Mode.EXCLUSIVE
                dockerTemplate.numExecutors = 100
                dockerTemplate.launcher = dockerComputerJNLPLauncher
                dockerTemplate.removeVolumes = temp.removeVolumes ? temp.removeVolumes.toBoolean() : false
                return dockerTemplate
            },
            serverUrl,
            "100", //containerCapStr,
            0, //connectTimeout,
            0, //readTimeout,
            credentialsId,
            null //version,
        )
    }
}

def setup(config){
    config = config ?: [:]
    def clouds = config.collect{k,v ->
        def cloudConfig = [id: k] << v
        switch(v.type){
            case 'docker':
                return dockerCloud(cloudConfig)
        }
    }.grep().each{ cloud ->
        def old = Jenkins.instance.clouds.find{ it.name == cloud.name}
        if(old){
            Jenkins.instance.clouds.remove(old)
        }
        Jenkins.instance.clouds.add(cloud)
    }
    Jenkins.instance.save()
}

return this
