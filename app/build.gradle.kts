import com.android.build.api.dsl.ApplicationDefaultConfig
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mapboxtest"
    compileSdk = 35

    val secrets = Properties().apply {
        load(project.rootProject.file("secrets.properties").inputStream())
    }

    fun ApplicationDefaultConfig.secretConfigField(name: String, key: String = name) {
        buildConfigField("String", name, "\"${secrets.getProperty(key)}\"")
    }

    defaultConfig {
        applicationId = "com.example.mapboxtest"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        secretConfigField("API_KEY")
        secretConfigField("AUTH_TARGET")
        secretConfigField("INGESTER_TARGET")
        secretConfigField("STREAM_TARGET")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    }

    flavorDimensions += listOf("location", "map")
    productFlavors {
        create("dfw") {
            dimension = "location"
            buildConfigField("String", "BRAND_NAME", "\"DFW\"")
        }

        create("mohegan") {
            dimension = "location"
            buildConfigField("String", "BRAND_NAME", "\"Mohegan\"")
        }

        create("mapbox") {
            dimension = "map"
            resValue("string", "mapbox_access_token", secrets.getProperty("MAPBOX_API_KEY").toString())
        }

        create("google") {
            dimension = "map"
            manifestPlaceholders["MAPS_API_KEY"] = secrets.getProperty("GOOGLE_MAPS_API_KEY").toString()
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.productFlavors.containsAll(listOf("map" to "google", "location" to "dfw"))) {
            variantBuilder.enable = false
        }
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    "mapboxImplementation"(libs.mapbox.maps)
    "mapboxImplementation"(libs.mapbox.compose)

    "googleImplementation"(libs.google.maps)
    "googleImplementation"(libs.google.maps.compose)

    implementation(libs.android.sdk)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.accompanist.permissions)
}