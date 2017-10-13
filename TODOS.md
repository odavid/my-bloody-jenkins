jenkins.slaves.DefaultJnlpSlaveReceiver.disableStrictVerification=true

handle JNLP Agent protocols - seems JNLP4 does not work with kubernetes... when changing to JNLP3, it works

add support for pfx cert in credentials

change behaviour of tunnel to be jenkinsUrl, in kubernetes it does not work

remove swarm plugin from plugins list

fix kubernetes - use only jnlp in container name

make a note about kubernetes plugin default commands from the ui

container cap and clock issue in kubernetes
