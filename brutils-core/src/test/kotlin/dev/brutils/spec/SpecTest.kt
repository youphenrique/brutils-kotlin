package dev.brutils.spec

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test

class SpecTest {
    @Test
    fun `loads the supported spec version of each module`() {
        val cpf = Spec.load("cpf")

        cpf["module"]?.jsonPrimitive?.content shouldBe "cpf"
        cpf["specVersion"]?.jsonPrimitive?.int shouldBe 1
    }
}
