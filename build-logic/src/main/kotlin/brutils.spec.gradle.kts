import dev.brutils.gradle.SyncSpec

// Applied to the root project only; library tests depend on :syncSpec through brutils.kotlin-library.
tasks.register<SyncSpec>("syncSpec") {
    description = "Copies the brutils conformance spec into build/brutils-spec."
    ref = providers.gradleProperty("brutils.spec.ref")
    localDir = providers.gradleProperty("brutils.spec.dir").map { layout.projectDirectory.dir(it) }
    outputDir = layout.buildDirectory.dir("brutils-spec")
}
