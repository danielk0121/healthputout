plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("idea")
}

android {
    namespace = "dev.danielk.healthputout"
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.danielk.healthputout"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // IntelliJ IDEA가 Gradle의 빌드 결과물(.class) 폴더를 인식하도록 설정
    idea {
        module {
            inheritOutputDirs = true // 다시 true로 돌려 기본 구조를 잡습니다.
        }
    }
}

dependencies {
    // 1. 삼성 헬스 SDK 로드 (libs 폴더의 aar 인식)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("com.google.code.gson:gson:2.9.0")

    // 2. 핵심 라이브러리 정리 (중복 제거 및 버전 고정)
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1") // SDK 호환성을 위해 1.6.1 권장
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1") // 버전은 프로젝트에 맞춰 조정 가능
}