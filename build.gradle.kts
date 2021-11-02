// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.fabric.io/public")
		maven("https://maven.google.com")
	}
	dependencies {
		classpath("com.android.tools.build:gradle:7.0.3")
		classpath(kotlin("gradle-plugin", version = "1.5.31"))
		classpath(kotlin("serialization", version = "1.5.31"))
		classpath("com.google.gms:google-services:4.3.10")
		classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.0")
		// NOTE: Do not place your application dependencies here; they belong
		// in the individual module build.gradle files
	}
}

allprojects {
	repositories {
		maven("https://maven.google.com")
		google()
		mavenCentral()
	}
	val versionMajor by extra(1)
	val versionMinor by extra(0)
	val versionPatch by extra(1)
	val versionClassifier by extra("SNAPSHOT")

	project.extra.apply {
		set("versionCode", versionMajor * 10000 + versionMinor * 100 + versionPatch)
		set("versionName", getVersionName())
	}
}

task<Delete>("clean") {
	delete(rootProject.buildDir)
}

fun getVersionName(): String {
	val versionMajor by extra(1)
	val versionMinor by extra(0)
	val versionPatch by extra(1)
	var versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

	val versionClassifier: String? by extra("SNAPSHOT")
	if (versionClassifier != null && versionClassifier!!.isNotEmpty()) {
		versionName = "$versionName-$versionClassifier"
	}
	return versionName
}

subprojects {
	project.configurations.all {
		resolutionStrategy.eachDependency {
			if (this.requested.group == "com.android.support"
				&& !this.requested.name.contains("multidex")
			) {
				this.useVersion("$rootProject.supportVersion")
			}
		}
	}
}