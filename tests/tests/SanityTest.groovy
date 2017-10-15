def printActivePlugins(){
	jenkins.model.Jenkins.instance.pluginManager.activePlugins.sort{it.shortName}.each{
		out.println it.shortName + ':' + it.version
	}
}

def testNoFailedPlugins(){
	assert jenkins.model.Jenkins.instance.pluginManager.failedPlugins.empty
}

testNoFailedPlugins()