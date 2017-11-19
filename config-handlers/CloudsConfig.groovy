import hudson.model.Node
import hudson.slaves.JNLPLauncher
import hudson.plugins.sshslaves.SSHConnector
import jenkins.model.Jenkins

import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import com.nirima.jenkins.plugins.docker.strategy.*

import com.cloudbees.jenkins.plugins.amazonecs.*
import static com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.*

import org.csanchez.jenkins.plugins.kubernetes.*
import org.csanchez.jenkins.plugins.kubernetes.volumes.*
import org.csanchez.jenkins.plugins.kubernetes.volumes.workspace.*

def asInt(value, defaultValue=0){
    return value ? value.toInteger() : defaultValue
}
def asBoolean(value, defaultValue=false){
    return value != null ? value.toBoolean() : defaultValue
}

def pathToVolumeName(path){
    path.split('/').collect{ org.apache.commons.lang.StringUtils.capitalize(it)}.join('').replaceAll('\\.', '_')
}

def parseContainerVolume(volume, closure){
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
        vol_name = pathToVolumeName(host_path)
    } else if(host_path){
        vol_name = pathToVolumeName(host_path)
    }else{
        vol_name = pathToVolumeName(container_path)
    }
    return closure(
        vol_name,
        host_path,
        container_path,
        read_only
    )
}

def dockerCloud(config){
    config.with{
        def dockerCloud = new DockerCloud(
            id,
            templates?.collect{ temp ->
                def dockerTemplate = new DockerTemplate(
                    new DockerTemplateBase(
                        temp.image,
                        temp.pullCredentialsId?:'',
                        temp.dns?.join(' '),
                        temp.network?:'',
                        temp.command?:'',
                        temp.volumes?.join('\n'),
                        temp.volumesFrom?.join(' '),
                        temp.environment?.collect{k,v -> "${k}=${v}"}?.join("\n"),
                        temp.lxcConf?:'',
                        temp.hostname?:'',
                        asInt(temp.memory),
                        asInt(temp.memorySwap),
                        asInt(temp.cpu),
                        temp.ports?.join(' ') ?:'',
                        asBoolean(temp.bindAllPorts),
                        asBoolean(temp.privileged),
                        asBoolean(temp.tty),
                        temp.macAddress
                    ),
                    temp.labels?.join(' '),
                    temp.remoteFs?:'',
                    temp.remoteFsMapping?:'',
                    temp.instanceCap?.toString() ?: "",
                    []
                )
                dockerTemplate.mode = Node.Mode.EXCLUSIVE
                dockerTemplate.numExecutors = asInt(temp.numExecutors, 1)
                dockerTemplate.connector = new io.jenkins.docker.connector.DockerComputerJNLPConnector(
                    new JNLPLauncher(tunnel, temp.jvmArgs)
                )
                dockerTemplate.connector.user = config.jnlpUser?:''
                dockerTemplate.removeVolumes = asBoolean(temp.removeVolumes)
                dockerTemplate.dockerTemplateBase.extraHosts = extraHosts?:[]
                return dockerTemplate
            },
            new org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint(
                dockerHostUri,
                credentialsId
            ),
            asInt(containerCap, 100),
            asInt(connectTimeout),
            asInt(readTimeout),
            apiVersion?:'',
            dockerHostname?:''
        )
        dockerCloud.exposeDockerHost = asBoolean(exposeDockerHost, true)
        return dockerCloud
    }
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
                    asInt(temp.memory),
                    asInt(temp.memoryReservation),
                    asInt(temp.cpu),
                    asBoolean(temp.privileged),
                    temp.logDriverOptions?.collect{ k,v -> new LogDriverOption(k,v) },
                    temp.environment?.collect{ k, v -> new EnvironmentEntry(k,v) },
                    temp.extraHosts?.collect { k, v -> new ExtraHostEntry(k,v) },
                    temp.volumes?.collect { vol -> parseContainerVolume(vol){
                        vol_name, host_path, container_path,read_only ->
                            new MountPointEntry(vol_name, host_path, container_path,read_only)
                        }
                    }
                )
                ecsTemplate.jvmArgs = temp.jvmArgs
                ecsTemplate.entrypoint = temp.entrypoint
                ecsTemplate.logDriver = temp.logDriver
                ecsTemplate.dnsSearchDomains = temp.dns
                return ecsTemplate
            },
            credentialsId ?: '',
            cluster,
            region,
            jenkinsUrl,
            asInt(connectTimeout)
        )
        ecsCloud.tunnel = tunnel
        return ecsCloud
    }
}

def kubernetesCloud(config){
    config.with{
        def kubernetesCloud = new KubernetesCloud(
            id,
            templates?.collect{ temp ->
                def name = temp.name ? temp.name : temp.labels?.join('-')

                def containerTemplate = new ContainerTemplate('jnlp', temp.image)
                containerTemplate.command = temp.command ?: ''
                containerTemplate.args = temp.args ?: ''
                containerTemplate.ttyEnabled = asBoolean(temp.tty)
                containerTemplate.workingDir = temp.remoteFs ?: ContainerTemplate.DEFAULT_WORKING_DIR
                containerTemplate.privileged = asBoolean(temp.privileged)
                containerTemplate.alwaysPullImage = asBoolean(temp.alwaysPullImage)
                containerTemplate.envVars = temp.environment?.collect{ k, v -> new ContainerEnvVar(k,v) } ?: []
                containerTemplate.ports = temp.ports?.collect{ portMapping ->
                    def parts = portMapping.split(':')
                    def hostPort = parts.size() > 1 ? parts[0] : null
                    def containerPort = parts.size() > 1 ? parts[1] : parts[0]
                    return PortMapping(null, containerPort?.toInteger(), hostPort?.toInteger())
                }
                containerTemplate.resourceRequestMemory = temp.resourceRequestMemory
                containerTemplate.resourceRequestCpu = temp.resourceRequestCpu
                containerTemplate.resourceLimitMemory = temp.resourceLimitMemory
                containerTemplate.resourceLimitCpu = temp.containerTemplate
                containerTemplate.livenessProbe = temp.livenessProbe ? new ContainerLivenessProbe(
                    temp.livenessProbe.execArgs,
                    asInt(temp.livenessProbe.timeoutSeconds),
                    asInt(temp.livenessProbe.initialDelaySeconds),
                    asInt(temp.livenessProbe.failureThreshold),
                    asInt(temp.livenessProbe.periodSeconds),
                    asInt(temp.livenessProbe.successThreshold)
                ) : null

                def podTemplate = new PodTemplate()
                podTemplate.name = name
                podTemplate.containers << containerTemplate
                podTemplate.namespace = temp.namespace
                podTemplate.label = temp.labels?.join(' ')
                podTemplate.nodeUsageMode = Node.Mode.EXCLUSIVE
                podTemplate.inheritFrom = temp.inheritFrom
                podTemplate.nodeSelector = temp.nodeSelector
                podTemplate.serviceAccount = serviceAccount
                podTemplate.slaveConnectTimeout = asInt(slaveConnectTimeout, PodTemplate.DEFAULT_SLAVE_JENKINS_CONNECTION_TIMEOUT)
                podTemplate.instanceCap = asInt(instanceCap, -1)
                podTemplate.imagePullSecrets = imagePullSecrets?.collect{secretName -> new PodImagePullSecret(secretName)}
                def simplePodVolumes = temp.volumes?.collect { vol -> parseContainerVolume(vol){
                    vol_name, host_path, container_path,read_only ->
                        if(host_path){
                            return new HostPathVolume(host_path, container_path)
                        }else{
                            return new EmptyDirVolume(container_path, false)
                        }
                    }
                }
                if(simplePodVolumes){
                    podTemplate.volumes.addAll(simplePodVolumes)
                }
                return podTemplate
            },
            serverUrl,
            namespace ?: 'default',
            jenkinsUrl,
            containerCap ? containerCap.toString() : "",
            asInt(connectTimeout),
            asInt(readTimeout),
            asInt(retentionTimeout, KubernetesCloud.DEFAULT_RETENTION_TIMEOUT_MINUTES)
        )
        kubernetesCloud.jenkinsTunnel = tunnel
        kubernetesCloud.credentialsId = credentialsId
        kubernetesCloud.skipTlsVerify = asBoolean(skipTlsVerify)
        kubernetesCloud.serverCertificate = serverCertificate
        kubernetesCloud.maxRequestsPerHostStr = maxRequestsPerHost ? maxRequestsPerHost.toString() : null
        kubernetesCloud.defaultsProviderTemplate = defaultsProviderTemplate

        return kubernetesCloud
    }
}

def setup(config){
    def env = System.getenv()
    def jenkins_ip_for_slaves = env['JENKINS_IP_FOR_SLAVES']
    def jenkins_http_port_for_slaves = env['JENKINS_HTTP_PORT_FOR_SLAVES'] ?: 8080
    def jenkinsUrl = jenkins_ip_for_slaves ? "http://${jenkins_ip_for_slaves}:${jenkins_http_port_for_slaves}".toString() : null

    config = config ?: [:]
    def clouds = config.collect{k,v ->
        def cloudConfig = [id: k] << v
        cloudConfig.jenkinsUrl = jenkinsUrl && (cloudConfig.jenkinsUrl == null) ? jenkinsUrl : cloudConfig.jenkinsUrl
        switch(v.type){
            case 'docker':
                return dockerCloud(cloudConfig)
            case 'ecs':
                return ecsCloud(cloudConfig)
            case 'kubernetes':
                return kubernetesCloud(cloudConfig)
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
