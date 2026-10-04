plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// Local and snapshot builds must never compete with an immutable Maven Central
// version: publishToMavenLocal always uses the -SNAPSHOT-flavoured line.
val isPublishingToMavenLocal = gradle.startParameter.taskNames.any {
    val name = it.substringAfterLast(':')
    name == "publishToMavenLocal" || name.endsWith("PublicationToMavenLocal")
}

val defaultPublicationVersion = if (isPublishingToMavenLocal) {
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

    extra["signPublications"] = canSign
}
