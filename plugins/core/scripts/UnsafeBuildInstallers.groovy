/*
 * N.B. it is not safe to run this script for plugins because plugin.xml may be
 *   stale.  instead you should run `../core/do/build_installers`.
 */

includeTargets << grailsScript("Init") << grailsScript("War")

def envCheck = {
	if(grailsSettings.grailsEnv != 'production') {
		def middleLine = "# !! WARNING !! You are building for $grailsSettings.grailsEnv !! WARNING !! #"
		println '#' * middleLine.size()
		println middleLine
		println '#' * middleLine.size()
	}
}

def isSet(String var) {
	return getValue(var)? true: false
}

def getValue(String var) {
	return System.properties."frontlinesms2.build.$var"
}

def getValueAsBoolean(String var, boolean defaultValue) {
	if(isSet(var)) return Boolean.parseBoolean(getValue(var))
	else return defaultValue
}

def isWindows() {
	System.properties.'os.name'.toLowerCase().contains('windows')
}

def mvn() {
	return isWindows()? 'mvn.bat': 'mvn'
}

target(main: 'Build installers for various platforms.') {
	envCheck()
	if(!getValueAsBoolean('confirmNotProd', grailsSettings.grailsEnv == 'production')) {
		input('Press Return to continue building...')
	}
	if(getValueAsBoolean('skipWar', false)) {
		if(grailsSettings.grailsEnv == 'production') {
			println "CANNOT SKIP WAR BUILD FOR PRODUCTION"
			depends(clean, war)
		} else {
			println "Skipping WAR build..."
			depends(clean)
		}
	} else depends(clean, war)
	if(!isWindows()) if(getValueAsBoolean('compress', grailsSettings.grailsEnv == 'production')) {
		println 'Forcing compression of installers...'
		exec executable:'../core/do/enable_installer_compression'
	} else {
		println 'Disabling compression of installers...'
		exec executable:'../core/do/disable_installer_compression'
	}
	def appName = metadata.'app.name'
	def appVersion = metadata.'app.version'
	println "Building $appName, v$appVersion"
	def webappTempDir = '../core/install/src/web-app'
	delete(dir:webappTempDir)
	unzip(src:"target/${appName}-${appVersion}.war", dest:webappTempDir)

	// Build instal4j custom code JAR
	exec(dir:'../core/install', output:'install4j-custom-classes.maven.log', executable:mvn(), args) {
		arg value:'-f'
		arg value:'install4j-custom-classes.pom.xml'
		arg value:'clean'
		arg value:'package'
	}
	
	// Build installers
	exec(dir:'../core/install', output:'install4j.maven.log', executable:mvn(), args) {
		arg value:"-Dbuild.version=$appVersion"
		arg value:"-Dfrontlinesms.flavour=$appName"
		arg value:'clean'
		arg value:'package'
	}

	// Make sure that linux installer is executable
	chmod dir:'../core/install/target/install4j', includes:'*.sh', type:'file', perm:'a+x'

	envCheck()
}

setDefaultTarget(main)