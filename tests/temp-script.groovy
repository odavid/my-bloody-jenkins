jenkins.model.Jenkins.instance.pluginManager.activePlugins.sort{it.shortName}.each{
	out.println it.shortName + ':' + it.version
}
assert jenkins.model.Jenkins.instance.pluginManager.failedPlugins.empty
