def printActivePlugins(){
	jenkins.model.Jenkins.instance.pluginManager.activePlugins.sort{it.shortName}.each{
		out.println it.shortName + ':' + it.version
	}
}

def printLatestVersionsOfPlugins(){
	def updateSite = jenkins.model.Jenkins.getInstance().getUpdateCenter().getById('default')
	jenkins.model.Jenkins.instance.pluginManager.activePlugins.sort{it.shortName}.each{
		def p = updateSite.getPlugin(it.shortName)
		def version = p ? p.version : it.version
		out.println "${it.shortName}:${version}"
	}
}

def testNoFailedPlugins(){
	assert jenkins.model.Jenkins.instance.pluginManager.failedPlugins.empty : "There are failed plugins"
}

testNoFailedPlugins()