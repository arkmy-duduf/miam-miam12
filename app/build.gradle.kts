﻿plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }

android {
  namespace = "com.tony.mealstock"
  compileSdk = 34
  defaultConfig { applicationId = "com.tony.mealstock"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "1.0" }
  buildFeatures { viewBinding = true }
  compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
  kotlinOptions { jvmTarget = "17" }
  buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.recyclerview:recyclerview:1.3.2")
  implementation("androidx.fragment:fragment-ktx:1.8.3")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

  implementation("androidx.room:room-runtime:2.6.1")
  kapt("androidx.room:room-compiler:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")

  implementation("androidx.camera:camera-core:1.3.4")
  implementation("androidx.camera:camera-camera2:1.3.4")
  implementation("androidx.camera:camera-lifecycle:1.3.4")
  implementation("androidx.camera:camera-view:1.4.0")

  implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")

  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

  implementation("com.github.bumptech.glide:glide:4.16.0")
  kapt("com.github.bumptech.glide:compiler:4.16.0")
}

