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

import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.LogDriverOption
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.EnvironmentEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.ExtraHostEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.MountPointEntry


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

def pathToEcsVolumeName(path){
    path.split('/').collect{ org.apache.commons.lang.StringUtils.capitalize(it)}.join('').replaceAll('\\.', '_')
}
def parseEcsVolume(volume){
    def parts = volume.split(':')
    def host_path = null
    def container_path = null
    def read_only = false

    if(parts.size() == 1){
        container_path = parts[0]
    }
    if(parts.size() == 2 && parts[1] != 'ro'){
        host_path = parts[0]
        container_path = parts[1]
    }
    if(parts.size() == 2 && parts[1] == 'ro'){
        container_path = parts[0]
        read_only = true
    }
    if(parts.size() == 3 && parts[2] == 'ro'){
        host_path = parts[0]
        container_path = parts[1]
        read_only = true
    }
    if(parts.size() == 3 && parts[2] == 'rw'){
        host_path = parts[0]
        container_path = parts[1]
    }

    if(!host_path && !container_path){
        throw new RuntimeException("Invalid volume declaration: ${volume}")
    }

    if(host_path && (host_path[0] in ['~', '.'] || host_path[0] != '/' && '/' in host_path)){
        throw new RuntimeException("Not supported volume declaration: ${volume}, host path must be absolute")
    }

    if(host_path && host_path[0] == '/'){
        vol_name = pathToEcsVolumeName(host_path)
    } else if(host_path){
        vol_name = pathToEcsVolumeName(host_path)
    }else{
        vol_name = pathToEcsVolumeName(container_path)
    }
    return new MountPointEntry(
        vol_name,
        host_path,
        container_path,
        read_only
    )

}

def ecsCloud(config){
    config.with{
        def ecsCloud = new ECSCloud(
            id,
            templates?.collect{ temp ->
                def ecsTemplate = new ECSTaskTemplate(
                    temp.name ? temp.name : temp.labels?.join('-'),
                    temp.labels?.join(' '),
                    temp.image,
                    temp.remoteFs,
                    temp.memory ? temp.memory.toInteger() : 0,
                    temp.memoryReservation ? temp.memoryReservation.toInteger() : 0,
                    temp.cpu ? temp.cpu.toInteger() : 0,
                    temp.privileged ? temp.privileged.toBoolean() : false,
                    temp.logDriverOptions?.collect{ k,v -> new LogDriverOption(k,v) },
                    temp.environment?.collect{ k, v -> new EnvironmentEntry(k,v) },
                    temp.extraHosts?.collect { k, v -> new ExtraHostEntry(k,v) },
                    temp.volumes?.collect {vol -> parseEcsVolume(vol) }
                )
                ecsTemplate.jvmArgs = temp.vmargs
                ecsTemplate.entrypoint = temp.entrypoint
                ecsTemplate.logDriver = temp.logDriver
                ecsTemplate.dnsSearchDomains = temp.dns
                return ecsTemplate
            },
            credentialsId ?: '',
            cluster,
            region,
            jenkinsUrl,
            slaveTimoutInSeconds ? slaveTimoutInSeconds.toInteger() : 0
        )
        ecsCloud.tunnel = tunnel
        return ecsCloud
    }
}

def setup(config){
    config = config ?: [:]
    def clouds = config.collect{k,v ->
        def cloudConfig = [id: k] << v
        switch(v.type){
            case 'docker':
                return dockerCloud(cloudConfig)
            case 'ecs':
                return ecsCloud(cloudConfig)
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
