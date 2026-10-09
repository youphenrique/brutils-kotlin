import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("brutils.publishing")
}

// Baseline: Java 17, the floor of Spring Boot 4, Quarkus 3 and Micronaut 4. Compiling against the JDK 17 API
// (not just its bytecode level) keeps newer-JDK APIs out of the library whatever JDK runs the build.
val javaRelease = 17

java {
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaRelease
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaRelease.toString())
        freeCompilerArgs.add("-Xjdk-release=$javaRelease")
        allWarningsAsErrors = true
    }

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "dev.brutils.${project.name.removePrefix("brutils-")}")
    }
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("kotest-assertions-core").get())
    testImplementation(libs.findLibrary("kotlinx-serialization-json").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

val specDir = isolated.rootProject.projectDirectory.dir("build/brutils-spec")

tasks.test {
    useJUnitPlatform()
    dependsOn(":syncSpec")
    inputs.dir(specDir).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("brutilsSpec")
    systemProperty("brutils.spec.dir", specDir.asFile.absolutePath)
}
