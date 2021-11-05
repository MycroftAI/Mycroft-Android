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
	id("com.android.library")
	kotlin("android")
}

android {
	compileSdk = 29
	buildToolsVersion = "31.0.0"

	defaultConfig {
		minSdk = 19
		targetSdk = 29

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures {
		dataBinding = true
	}

	buildTypes {
		named("release") {
			isMinifyEnabled = false

			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}
	}
}

dependencies {
	implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))
	androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
	implementation("androidx.appcompat:appcompat:1.3.1")
	testImplementation("junit:junit:4.13.2")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
	implementation("androidx.annotation:annotation:1.2.0")

	implementation(group = "com.google.android.material", name = "material", version = "1.4.0")
}
