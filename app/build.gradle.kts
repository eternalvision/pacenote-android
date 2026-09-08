import java.io.FileInputStream
import java.util.Properties
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.alexander.pacenote"
    compileSdk = 36

    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                val storeFilePath = keystoreProperties["storeFile"] as String
                val storeFileCandidate = rootProject.file(storeFilePath)
                require(storeFileCandidate.exists()) {
                    "Release keystore file not found: $storeFileCandidate"
                }
                storeFile = storeFileCandidate
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.alexander.pacenote"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        disable += setOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "NewerVersionAvailable",
        )
    }
}

hilt {
    enableAggregatingTask = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*_Factory*",
                    "*_HiltModules*",
                )
            }
            includes {
                classes(
                    "com.alexander.pacenote.data.local.RoomLocalResultDataSource*",
                    "com.alexander.pacenote.data.local.SportsResultEntity*",
                    "com.alexander.pacenote.data.remote.InMemoryRemoteResultDataSource*",
                    "com.alexander.pacenote.data.repository.DefaultSportsResultRepository*",
                    "com.alexander.pacenote.domain.model.*",
                    "com.alexander.pacenote.domain.usecase.*",
                    "com.alexander.pacenote.domain.validation.*",
                    "com.alexander.pacenote.presentation.create.CreateResultState*",
                    "com.alexander.pacenote.presentation.create.CreateResultViewModel*",
                    "com.alexander.pacenote.presentation.results.ResultsState*",
                    "com.alexander.pacenote.presentation.results.ResultsViewModel*",
                )
            }
        }
        variant("debug") {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
            verify {
                rule("logic line coverage per class") {
                    groupBy = GroupingEntityType.CLASS
                    minBound(100, coverageUnits = CoverageUnit.LINE)
                }
                rule("logic branch coverage") {
                    minBound(100, coverageUnits = CoverageUnit.BRANCH)
                }
                rule("logic instruction coverage") {
                    minBound(100, coverageUnits = CoverageUnit.INSTRUCTION)
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
