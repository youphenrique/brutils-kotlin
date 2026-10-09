plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    // Uploads land in the Central Portal for a manual release; nothing goes live from CI alone.
    publishToMavenCentral(automaticRelease = false)
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates(group.toString(), name, version.toString())

    pom {
        name = project.name
        description = provider { project.description }
        url = "https://github.com/youphenrique/brutils-kotlin"
        inceptionYear = "2026"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "youphenrique"
                name = "Paulo Henrique"
                url = "https://github.com/youphenrique"
            }
        }
        scm {
            url = "https://github.com/youphenrique/brutils-kotlin"
            connection = "scm:git:https://github.com/youphenrique/brutils-kotlin.git"
            developerConnection = "scm:git:ssh://git@github.com/youphenrique/brutils-kotlin.git"
        }
    }
}
