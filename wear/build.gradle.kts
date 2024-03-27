plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zjael.juddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zjael.juddy"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val composeVersion = "1.0.4"
val wearVersion = "1.2.0"
val androidxActivity = "1.8.2"

dependencies {
    // Is this needed?
    implementation("com.google.android.gms:play-services-wearable:18.1.0")

    implementation("androidx.core:core-ktx:1.12.0")

    // Kotlin components
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.activity:activity-ktx:$androidxActivity")
    implementation("androidx.activity:activity-compose:$androidxActivity")

    // Compose components
    //implementation("androidx.compose.compiler:compiler:1.5.8")
    implementation("androidx.wear.compose:compose-foundation:1.2.1")
    implementation("androidx.wear.compose:compose-material:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")

    implementation("com.google.android.horologist:horologist-compose-layout:0.5.7")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Material components
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Wear libraries
    implementation ("androidx.wear:wear:1.3.0")

    // Watch face specific libraries
    implementation ("androidx.wear.watchface:watchface:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-client:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-data:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-editor:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-style:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-complications-rendering:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-complications-data:$wearVersion")
    implementation ("androidx.wear.watchface:watchface-complications-data-source:$wearVersion")
}