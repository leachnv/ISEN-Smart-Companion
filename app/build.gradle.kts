plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.isen.chanvillard.isensmartcompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.isen.chanvillard.isensmartcompanion"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ✅ Ajout de Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // ✅ Ajout de la navigation Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Convertisseur Gson
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0") // Vérifie la dernière version sur la doc officielle
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0") // Vérifie la version la plus récente !
    implementation (libs.androidx.room.runtime)  // Vérifiez la version la plus récente

    implementation (libs.androidx.room.ktx)  // Extensions Kotlin pour Room
    val compose_version = "1.4.0"  // Exemple de version de Compose
    val material3_version = "1.0.1"  // Exemple de version de Material3
    implementation ("androidx.compose.ui:ui:${compose_version}")
    implementation ("androidx.compose.material3:material3:${material3_version}")
    implementation ("androidx.compose.foundation:foundation:${compose_version}")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
