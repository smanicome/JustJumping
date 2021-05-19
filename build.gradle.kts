import com.android.build.gradle.internal.tasks.manifest.mergeManifestsForApplication
import com.soywiz.korge.gradle.*

buildscript {
	val korgePluginVersion: String by project

	repositories {
		mavenLocal()
		maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
		maven { url = uri("https://plugins.gradle.org/m2/") }
		mavenCentral()
		google()
	}
	dependencies {
		classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
	}
}

apply<KorgeGradlePlugin>()


korge {
	id = "fr.umlv.todogame.justjumping"
	name = "Just Jumping"
	orientation = Orientation.PORTRAIT
	fullscreen = true
	icon = File(rootDir, "icon.png")


// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets
	targetAndroidDirect()
}