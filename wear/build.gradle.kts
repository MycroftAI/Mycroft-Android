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
	kotlin("android")
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
	}
	buildTypes {
		named("release") {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}
	}
	productFlavors {
	}
}

dependencies {
	implementation(fileTree("include" to arrayOf("*.jar"), "dir" to "libs"))
	compileOnly("com.google.android.wearable:wearable:2.8.1")
	implementation("com.google.android.support:wearable:2.8.1")
	implementation("com.google.android.gms:play-services-wearable:17.1.0")
	implementation(project(":shared"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
}
