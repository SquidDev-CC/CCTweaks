# CCTweaks [![Build Status](https://travis-ci.org/SquidDev-CC/CC-Tweaks.svg?branch=master)](https://travis-ci.org/SquidDev-CC/CC-Tweaks)
Miscellaneous things for ComputerCraft

## Features
 - Config items including:
	 - Whitelist globals (get the `debug` API).
	 - Blacklist turtle verbs - no more `turtle.inspect` if you don't like it.
	 - Change computer timeout
 - Turtle refuel using Redstone Flux
 - Computer upgrades - normal to advanced!
 - Debug Wand - add the debug API to any computer
 - LuaJC compiler - 2-5x performance increase
 - Networking API with multipart support

## Contributing
### Code and dependencies:
Run the following commands:

You'll need Git installed. If you are using Windows, replacing `./gradlew` with `gradlew.bat`
 - `git clone https://github.com/SquidDev-CC/CC-Tweaks`
 - `./gradlew build` This should download all dependencies. You can test with `./gradlew runClient`
 - If the above doesn't work, run  `./gradlew setupDevWorkspace` and `./gradlew setupDecompWorkspace`

Because of how the CC-Tweaks works, you cannot use the built-in IDE tasks to run this project, and will need
to follow the instructions below to set your IDE up.

### IDEA
If you use [Intellij IDEA](https://www.jetbrains.com/idea/) then do not run the `idea` task. Instead launch IDEA,
go to File -&gt; New -&gt; Project from existing sources. The select the `build.gradle` file from the CC-Tweaks folder.

This should import the gradle project. Create a run configuration for `runClient` 
(Run -&gt; Edit Configurations -&gt; New -&gt; Gradle, then set the project to be CC-Tweaks and 
the task to be `runClient`).

### Other IDEs
I use IDEA, so I know how to set that up. If you use Eclipse or Netbeans, you will need to install a Gradle plugin and
set it up to run the `runClient` task.

There is a Gradle plugin for Eclipse [here](http://marketplace.eclipse.org/content/gradle-integration-eclipse-44).

## Including in your own project
CC-Tweaks is available on Maven. To include it in your project you will need the following code:

```groovy
repositories {
	mavenCentral()
	// Required for ChickenBonesCore (runtime deobfuscationdeobfuscation) and ForgeMultipart
	maven {
		name = "chickenbones"
		url = "http://chickenbones.net/maven"
	}

	// Holds the main CCTweaks code
	maven {
		name = "squiddev"
		url = "http://maven.bonzodandd.co.uk"
	}

	// ComputerCraft source
	ivy {
		name = "computercraft"
		artifactPattern "http://addons-origin.cursecdn.com/files/2228/723/[module][revision].[ext]"
	}
}

dependencies {
	compile "org.squiddev:CCTweaks:1.7.10-INSERT VERSION HERE:dev"
}
```
