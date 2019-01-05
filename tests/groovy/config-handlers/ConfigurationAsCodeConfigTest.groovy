import org.yaml.snakeyaml.Yaml

handler = 'ConfigurationAsCode'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testJCasC(){
 	def config = new Yaml().load("""
unclassified:
  ## https://github.com/jenkinsci/configuration-as-code-plugin/blob/1f79326e902fe721a3a05077a7e46f98569804ff/demos/simple-theme-plugin/README.md
  simple-theme-plugin:
    elements:
      - cssUrl:
          url: "https://example.bogus/test.css"
      - cssText:
          text: ".testcss { color: red }"
      - jsUrl:
          url: "https://example.bogus/test.js"
      - faviconUrl:
          url: "https://vignette.wikia.nocookie.net/deadpool/images/6/64/Favicon.ico"
""")
    configHandler.setup(config)
    def simpleTheme = jenkins.model.Jenkins.instance.getExtensionList(org.codefirst.SimpleThemeDecorator)[0]
    assert simpleTheme.elements[0].url == 'https://example.bogus/test.css'
    assert simpleTheme.elements[1].text == '.testcss { color: red }'
    assert simpleTheme.elements[2].url == 'https://example.bogus/test.js'
}

testJCasC()