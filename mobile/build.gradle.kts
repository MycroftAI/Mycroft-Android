/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

plugins {
	id("com.android.application")
	id("com.google.firebase.crashlytics")
	kotlin("android")
	id("com.google.gms.google-services")
}
android {

	compileSdk = 29
	buildToolsVersion = "31.0.0"
	defaultConfig {
		applicationId = "mycroft.ai"
		minSdk = 19
		targetSdk = 29
		versionCode = project.ext.get("versionCode") as Int
		versionName = project.ext.get("versionName") as String

		testInstrumentationRunner = ("androidx.test.runner.AndroidJUnitRunner")
	}
	buildTypes {
		named("release") {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}
	}

	buildFeatures {
		dataBinding = true
		viewBinding = true
	}
}

repositories {
	google()
	mavenCentral()
	maven("https://maven.fabric.io/public")
}

dependencies {
	implementation(fileTree("include" to arrayOf("*.jar"), "dir" to "libs"))
	implementation("androidx.appcompat:appcompat:1.3.1")
	implementation("com.google.android.material:material:1.4.0")

	implementation("androidx.legacy:legacy-support-v4:1.0.0")

	// layout deps
	implementation("androidx.cardview:cardview:1.0.0")
	implementation("androidx.recyclerview:recyclerview:1.2.1")

	implementation("androidx.palette:palette:1.0.0")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
	implementation("com.google.firebase:firebase-crashlytics:18.2.4")
	implementation("com.google.firebase:firebase-analytics:20.0.0")

	// Unit test dependencies
	testImplementation("org.mockito:mockito-core:4.0.0")
	testImplementation("org.powermock:powermock-api-mockito:1.7.4")
	testImplementation("org.powermock:powermock-module-junit4-rule-agent:2.0.9")
	testImplementation("org.powermock:powermock-module-junit4-rule:2.0.9")
	testImplementation("org.powermock:powermock-module-junit4:2.0.9")
	testImplementation("junit:junit:4.13.2")
	// Instrumentation dependencies
	androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
	androidTestImplementation("androidx.test.ext:junit:1.1.3")
	androidTestImplementation("androidx.annotation:annotation:1.2.0")

	implementation("com.google.android.gms:play-services-wearable:17.1.0")
	wearApp(project(":wear"))
	implementation(project(":shared"))
	//kotlin
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
	//rxjava,rxandroid
	implementation("io.reactivex.rxjava2:rxjava:2.2.21")
	implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

	implementation("org.java-websocket:Java-WebSocket:1.5.2")

	implementation("androidx.legacy:legacy-support-core-utils:1.0.0")
}
