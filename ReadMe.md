# CCTweaks [![Build Status](https://travis-ci.org/SquidDev-CC/CC-Tweaks.svg?branch=minecraft-1.8.9)](https://travis-ci.org/SquidDev-CC/CC-Tweaks)
Miscellaneous changes for ComputerCraft

## Features

### Programming ([More up-to-date list here](https://github.com/SquidDev-CC/CCTweaks-Lua))
 - Custom computer timeout
 - Whitelist globals (such as debug)
 - Fix the binary strings (for fs, http, rednet and os.queueEvent)
 - TCP socket API (`socket`)
 - Compression API (`data`)
 - [LuaJC](https://github.com/SquidDev/luaj.luajc) compiler (compiles Lua code to Java for performance boost)
 - [Cobalt](https://github.com/SquidDev/Cobalt) VM (reentrant fork of LuaJ)
   - Custom termination handler
   - Several bugs fixed (any object error messages, string pattern matching, number format strings)
 - Return HTTP handle on failures
 - Allow getting headers from HTTP responses
 - API for adding custom APIs

### Turtles
 - Blacklist turtle verbs - no more `turtle.inspect` if you don't like it.
 - Turtle refuel using RF/EU
 - Turtle tool host - use any tool with turtles
 - Turtles named dinnerbone or Grumm turn upside down

### Networking
 - Networking API: add custom network components
 - Multidimensional modems (connect wired networks together)
 - Full block modems (connect to peripherals on 6 sides)
 - Multipart support
 - Item transport through OpenPeripheral
 - Beautiful network visualiser

### More
 - Computer upgrades - convert normal computers to advanced!
 - Debug Wand - add the debug API to any computer
 - Monitors emit light
 
## Contributing
### Code and dependencies:
You'll need Git installed. If you are using Windows, replace `./gradlew` with `gradlew.bat`
 - `git clone https://github.com/SquidDev-CC/CC-Tweaks`
 - `./gradlew build` This should download all dependencies. You can test with `./gradlew runClient`
 - To get the deobfuscated sources run `./gradlew setupDecompWorkspace`

Because of how the CCTweaks works, you may experience issues using the built in IDE tasks to run this project.

## Including in your own project
CCTweaks is available on Maven. To include it in your project you will need the following code:

```groovy
repositories {
	// Holds the main CCTweaks code
	maven {
    		name = "squiddev"
    		url = "https://dl.bintray.com/squiddev/maven"
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
