plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

android {
    namespace = "com.example.sailboatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sailboatapp"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}


dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.constraintlayout)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation("com.google.maps.android:maps-compose:5.0.1")

    // Optionally, you can include the Compose utils library for Clustering,
    // Street View metadata checks, etc.
    implementation("com.google.maps.android:maps-compose-utils:5.0.1")

    // Optionally, you can include the widgets library for ScaleBar, etc.
    implementation("com.google.maps.android:maps-compose-widgets:5.0.1")


    implementation(libs.compose.navigation)

    // General compose dependencies
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    // Other compose dependencies

    // Compose for Wear OS Dependencies
    implementation("androidx.wear.compose:compose-material:1.3.1")

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation("androidx.wear.compose:compose-foundation:1.3.1")

    // Wear OS preview annotations
    implementation("androidx.wear.compose:compose-ui-tooling:1.3.1")

    // If you are using Compose Navigation, use the Wear OS version (NOT THE MOBILE VERSION).
    // Uncomment the line below and update the version number.
    implementation("androidx.wear.compose:compose-navigation:1.3.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Retrofit with Scalar Converter
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    // Retrofit with Kotlin serialization Converter
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("org.mozilla.geckoview:geckoview-omni:100.0.20220425210429")

    implementation ("org.nanohttpd:nanohttpd:2.3.1")

    


}