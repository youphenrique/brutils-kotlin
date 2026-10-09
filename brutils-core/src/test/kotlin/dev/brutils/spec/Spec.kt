package dev.brutils.spec

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

internal fun JsonObject.obj(key: String): JsonObject = getValue(key).jsonObject

internal fun JsonObject.array(key: String): JsonArray = getValue(key).jsonArray

internal fun JsonObject.string(key: String): String = getValue(key).jsonPrimitive.content

/** The array at [key], each element as an object. */
internal fun JsonObject.objects(key: String): List<JsonObject> = array(key).map { it.jsonObject }

/** The array at [key], each element as a string. */
internal fun JsonObject.strings(key: String): List<String> = array(key).map { it.jsonPrimitive.content }
