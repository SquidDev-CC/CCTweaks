# CCTweaks [![Build Status](https://travis-ci.org/SquidDev-CC/CCTweaks.svg?branch=minecraft-1.12)](https://travis-ci.org/SquidDev-CC/CCTweaks)
Miscellaneous changes for ComputerCraft

## Important!
CCTweaks is a core mod and makes modifications to the internals of ComputerCraft. Whilst the mod is thoroughly tested there may be issues. If you encounter any bugs report them *here* and **not** on the ComputerCraft issue tracker. This will ensure a faster response time and will ensure you do not frustrate DanTwoHundred. 

Thanks :smile:!

## Features

### Programming ([More up-to-date list here](https://github.com/SquidDev-CC/CCTweaks-Lua))
 - Custom computer timeout
 - Whitelist globals (such as debug)
 - TCP socket API (`socket`)
 - Compression API (`data`)
 - [Cobalt](https://github.com/SquidDev/Cobalt) VM (reentrant fork of LuaJ)
   - Custom termination handler
   - Several bugs fixed (any object error messages, string pattern matching, number format strings)
   - Run multiple computers at once
 - API for adding custom APIs

### Turtles
 - Blacklist turtle verbs - no more `turtle.inspect` if you don't like it.
 - Turtle refuel using RF/EU/Tesla and Forge Energy
 - Turtle tool host - use any tool with turtles

### Networking
 - Networking API: add custom network components
 - Multidimensional modems (connect wired networks together)
 - Full block modems (connect to peripherals on 6 sides)
 - Multipart support
 - Beautiful network visualiser

### More
 - Computer upgrades - convert normal computers to advanced!
 - Debug Wand - add the debug API to any computer
 - Packet optimisations, reducing network traffic
 - Powerful server management commands, allowing monitoring, profiling and controlling computers.

## Contributing
### Code and dependencies:
You'll need Git installed. If you are using Windows, replace `./gradlew` with `gradlew.bat`
 - `git clone https://github.com/SquidDev-CC/CCTweaks`
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
}

dependencies {
	compile "org.squiddev:CCTweaks:${mc_version}-${cctweaks_version}:dev"
}
```
