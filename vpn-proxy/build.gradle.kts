import org.jetbrains.kotlin.cli.jvm.main

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ooimi.socket.proxy"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
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
        aidl = true
    }

//    sourceSets {
//        getByName("main") {
//            aidl.srcDirs("src/main/aidl")
//        }
//    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}