package dev.brutils.spec

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.io.path.Path
import kotlin.io.path.readText

/** Loads the language-neutral conformance cases synced by the root `syncSpec` task. */
internal object Spec {
    private val dir =
        Path(
            checkNotNull(System.getProperty("brutils.spec.dir")) {
                "brutils.spec.dir is not set; run the tests through Gradle."
            },
        )

    fun load(module: String): JsonObject =
        Json.parseToJsonElement(dir.resolve("$module/cases.json").readText()).jsonObject
}
