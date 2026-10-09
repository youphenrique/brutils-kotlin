plugins {
    id("brutils.kotlin-library")
}

description = "Jakarta Validation constraints for Brazilian documents, backed by brutils-core."

dependencies {
    api(projects.brutilsCore)
    api(libs.jakarta.validation.api)

    testImplementation(libs.hibernate.validator)
    testRuntimeOnly(libs.expressly)
}
