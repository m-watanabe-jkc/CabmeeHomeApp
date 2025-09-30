

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jvckenwood.cabmee.homeapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jvckenwood.cabmee.homeapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val ktlint: Configuration by configurations.creating

dependencies {
    // Android Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose Libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ViewModel
    implementation(libs.androidx.viemodel.compose)

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Material Icons Extended
    implementation(libs.androidx.compose.material.icons.extended)

    // Dagger Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)

    // DataStore
    implementation(libs.datastore)

    // Protocol Buffer
    implementation(libs.protobuf)

    // Retrofit2
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converterMoshi)

    // OkHttp
    implementation(libs.okhttp3)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Kotlin Result
    implementation(libs.kotlinResult)

    // Timber
    implementation(libs.timber)

    // Unit Test Libraries
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    // Android Test Libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Development Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Lint Libraries
    ktlint(libs.ktlint)
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("documents/api"))
}

val ktlintCheck by tasks.registering(JavaExec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args = listOf(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**"
    )
}

val ktlintFormat by tasks.registering(JavaExec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args = listOf(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**"
    )
}

tasks.check {
    dependsOn(ktlintCheck)
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                classes(
                    // Hilt による自動生成
                    "hilt_aggregated_deps.*",
                    "dagger.*",
                    "*_Factory",
                    "*_*Factory*",
                    "*.Hilt_*",
                    "*_HiltModules*"
                )
            }
        }
    }
}
