plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    explicitApi()
}

android {
    namespace = "io.github.jinlinahida.shirokowear.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    resourcePrefix = "shirokowear_"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.compose.ui)
    api(libs.compose.animation)
    api(libs.wear.compose.material3)
    api(libs.wear.compose.foundation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.graphics.shapes)

    testImplementation(libs.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.junit)
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "shirokowear-ui",
        version = project.version.toString()
    )

    // Central target is enabled through `mavenCentralPublishing=true` in
    // gradle.properties so this file stays valid across plugin versions.
    if (project.extra["signPublications"] == true) {
        signAllPublications()
    }

    pom {
        name.set("ShirokoWear UI")
        description.set("Wear OS design system: motion, haptics, containers and visual primitives.")
        inceptionYear.set("2026")
        url.set("https://github.com/jinlinahida/ShirokoWearUI")

        licenses {
            license {
                name.set("Apache-2.0 license")
                url.set("https://github.com/jinlinahida/ShirokoWearUI/blob/main/LICENSE")
            }
        }

        developers {
            developer {
                id.set("jinlinahida")
                name.set("jinlinahida")
            }
        }

        scm {
            url.set("https://github.com/jinlinahida/ShirokoWearUI")
            connection.set("scm:git:git://github.com/jinlinahida/ShirokoWearUI.git")
            developerConnection.set("scm:git:ssh://git@github.com/jinlinahida/ShirokoWearUI.git")
        }
    }
}
