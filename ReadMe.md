# CCTweaks [![Build Status](https://travis-ci.org/SquidDev-CC/CC-Tweaks.svg?branch=minecraft-1.8)](https://travis-ci.org/SquidDev-CC/CC-Tweaks)
Miscellaneous things for ComputerCraft

## Features
 - Config items including:
	 - Whitelist globals (get the `debug` API).
	 - Blacklist turtle verbs - no more `turtle.inspect` if you don't like it.
	 - Change computer timeout
	 - Monitor light levels
 - Turtle refuel using RF/EU
 - Computer upgrades - normal to advanced!
 - Debug Wand - add the debug API to any computer
 - LuaJC compiler - 2-5x performance increase
 - Networking API
	 - Multidimensional modems
	 - Full block modems
 - Turtle tool host - use any tool with turtles

## Contributing
### Code and dependencies:
You'll need Git installed. If you are using Windows, replacing `./gradlew` with `gradlew.bat`
 - `git clone https://github.com/SquidDev-CC/CC-Tweaks`
 - `./gradlew build` This should download all dependencies. You can test with `./gradlew runClient`
 - To get the deobfuscated sources run  `./gradlew setupDecompWorkspace`

Because of how the CCTweaks works, you may experience issues using the built in IDE tasks to run this project.

## Including in your own project
CCTweaks is available on Maven. To include it in your project you will need the following code:

```groovy
repositories {
	// Holds the main CCTweaks code
	maven {
		name = "squiddev"
		url = "http://maven.bonzodandd.co.uk"
	}

	// ComputerCraft
	ivy {
		name = "computercraft"
		artifactPattern "http://addons-origin.cursecdn.com/files/2272/212/[module][revision](.[ext])"
	}
}

dependencies {
	compile "org.squiddev:CCTweaks:${mc_version}-${cctweaks_version}:dev"
}
```
