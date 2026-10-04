import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.kover) apply false
}

// Local and snapshot builds must never compete with an immutable Maven Central
// version: publishToMavenLocal always uses the -SNAPSHOT-flavoured line.
val isPublishingToMavenLocal = gradle.startParameter.taskNames.any {
    val name = it.substringAfterLast(':')
    name == "publishToMavenLocal" || name.endsWith("PublicationToMavenLocal")
}

// `-PsnapshotPublication=true` (set by the snapshot workflow) publishes the
// SNAPSHOT line to GitHub Packages instead of Central.
val isPublishingSnapshot = providers.gradleProperty("snapshotPublication")
    .map { it.toBooleanStrict() }
    .getOrElse(false)

val defaultPublicationVersion = if (isPublishingToMavenLocal || isPublishingSnapshot) {
    libs.versions.mavenLocalVersion.get()
} else {
    libs.versions.version.get()
}

val publicationVersion = providers.gradleProperty("publicationVersion")
    .getOrElse(defaultPublicationVersion)

// Central requires signatures, but signing keys only exist in CI. Local and
// snapshot loops would be unusable if signing were mandatory.
val canSign = providers.gradleProperty("signingInMemoryKey").isPresent

allprojects {
    group = "io.github.jinlinahida"
    version = publicationVersion

    extra["signPublications"] = canSign && !isPublishingSnapshot

    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            if (isPublishingSnapshot) {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri(
                            "https://maven.pkg.github.com/" +
                                providers.environmentVariable("GITHUB_REPOSITORY")
                                    .getOrElse("jinlinahida/ShirokoWearUI"),
                        )
                        credentials {
                            username = providers.environmentVariable("GITHUB_ACTOR")
                                .getOrElse("jinlinahida")
                            password = providers.environmentVariable("GITHUB_TOKEN").orNull
                        }
                    }
                }
            }
        }
    }
}
