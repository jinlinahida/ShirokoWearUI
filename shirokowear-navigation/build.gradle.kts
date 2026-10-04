import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.kover)
}

kover {
    reports {
        filters {
            includes {
                classes(
                    "io.github.jinlinahida.shirokowear.navigation.ShirokoWearRouteKt*",
                    "io.github.jinlinahida.shirokowear.navigation.ShirokoWearIntroDirectionKt*",
                )
            }
        }
        verify {
            rule("Direction resolution stays covered") {
                minBound(80)
            }
        }
    }
}

kotlin {
    explicitApi()
}

android {
    namespace = "io.github.jinlinahida.shirokowear.navigation"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    resourcePrefix = "shirokowear_nav_"

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
    api(projects.shirokowear)
    api(libs.compose.ui)
    api(libs.compose.animation)
    api(libs.wear.compose.material3)

    testImplementation(libs.junit)
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "shirokowear-navigation",
        version = project.version.toString()
    )

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (project.extra["signPublications"] == true) {
        signAllPublications()
    }

    pom {
        name.set("ShirokoWear UI Navigation")
        description.set("Route contract, page transitions and ambient spotlight for Wear OS.")
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
