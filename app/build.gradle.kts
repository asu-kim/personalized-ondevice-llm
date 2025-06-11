plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.knowledgegraph"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.knowledgegraph"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CONTENT_PROVIDER_AUTHORITY", "\"com.example.knowledgegraph.kgprovider\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")
//    implementation("com.google.api-client:google-api-client-android:1.34.1")
//    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
//    implementation("com.google.http-client:google-http-client-gson:1.41.0")
//    implementation("com.google.api-client:google-api-client-gson:1.34.1")
//    implementation("com.google.apis:google-api-services-calendar:v3-rev305-1.25.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
//    implementation("com.github.sharkdeng:tokenizers:android-0.1.0")
//    implementation ("com.github.huggingface:tokenizers:Tag")
    implementation(libs.androidx.ui.text)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}