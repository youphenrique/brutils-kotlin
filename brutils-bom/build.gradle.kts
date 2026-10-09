plugins {
    `java-platform`
    id("brutils.publishing")
}

description = "Bill of materials aligning the versions of all brutils-kotlin modules."

dependencies {
    constraints {
        api(projects.brutilsCore)
        api(projects.brutilsJakarta)
    }
}
