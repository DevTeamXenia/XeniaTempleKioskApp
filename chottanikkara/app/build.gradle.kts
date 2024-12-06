    plugins {
        id("com.android.application")
        id("org.jetbrains.kotlin.android")
    }

    android {
        namespace = "com.xenia.templekiosk"
        compileSdk = 35

        buildFeatures {
            viewBinding =  true
        }

        defaultConfig {
            applicationId = "com.xenia.templekiosk"
            minSdk = 24
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildFeatures {
            dataBinding 
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

    }



    dependencies {

        implementation("androidx.core:core-ktx:1.15.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.constraintlayout:constraintlayout:2.2.0")
        implementation("androidx.activity:activity-ktx:1.9.3")
        implementation(files("libs/printer-lib-2.2.4.aar"))
        implementation("androidx.activity:activity:1.9.3")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

        implementation("com.journeyapps:zxing-android-embedded:4.1.0")

        implementation ("com.github.bumptech.glide:glide:4.13.2")

        implementation ("com.squareup.retrofit2:retrofit:2.11.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

        implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

        //di
        implementation ("io.insert-koin:koin-android:3.2.2")

    }