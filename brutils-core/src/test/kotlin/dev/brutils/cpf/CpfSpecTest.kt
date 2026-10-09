package dev.brutils.cpf

import dev.brutils.ValidationResult
import dev.brutils.spec.Spec
import dev.brutils.spec.array
import dev.brutils.spec.obj
import dev.brutils.spec.objects
import dev.brutils.spec.string
import dev.brutils.spec.strings
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

/** Runs the shared brutils-spec CPF cases; see https://github.com/youphenrique/brutils-spec. */
class CpfSpecTest {
    private val spec = Spec.load("cpf")

    @Test
    fun `public enums match the spec`() {
        CpfErrorCode.entries.map { it.name } shouldContainExactly spec.strings("errorCodes")
        CpfMaskMode.entries.map { it.value } shouldContainExactly spec.strings("maskModes")
    }

    @TestFactory
    fun `accepts each valid CPF in either shape`(): List<DynamicTest> =
        spec.obj("valid").objects("cases").map { case ->
            val raw = case.string("raw")
            val formatted = case.string("formatted")
            dynamicTest(raw) {
                for (value in listOf(raw, formatted)) {
                    assertSoftly {
                        Cpf.validate(value) shouldBe ValidationResult.Valid
                        Cpf.normalize(value) shouldBe raw
                        Cpf.format(value) shouldBe formatted
                        Cpf.formatAsYouType(value) shouldBe formatted
                        Cpf.mask(value) shouldBe case.string("masked")
                    }
                }
            }
        }

    @TestFactory
    fun `generates each valid CPF from its base digits`(): List<DynamicTest> =
        spec.obj("valid").objects("cases").map { case ->
            val raw = case.string("raw")
            val base = raw.take(9).map { it - '0' }
            dynamicTest(raw) {
                Cpf.generate(random = SequenceRandom(base)) shouldBe raw
                Cpf.generate(formatted = true, random = SequenceRandom(base)) shouldBe case.string("formatted")
            }
        }

    @TestFactory
    fun `rejects corruption of either check digit`(): List<DynamicTest> {
        val section = spec.obj("checkDigitCorruption")
        return spec.obj("valid").objects("cases").map { case ->
            val raw = case.string("raw")
            dynamicTest(raw) {
                for (position in listOf(9, 10)) {
                    for (replacement in '0'..'9') {
                        if (replacement == raw[position]) continue
                        val corrupted = raw.replaceRange(position, position + 1, replacement.toString())
                        for (value in listOf(corrupted, Cpf.format(corrupted))) {
                            Cpf.validate(value).errorCode() shouldBe section.string("errorCode")
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun `rejects repeated digits in either shape`(): List<DynamicTest> {
        val section = spec.obj("repeatedDigits")
        return section.strings("cases").map { raw ->
            dynamicTest(raw) {
                for (value in listOf(raw, Cpf.format(raw))) {
                    Cpf.validate(value).errorCode() shouldBe section.string("errorCode")
                }
            }
        }
    }

    @TestFactory
    fun `applies the malformed-string policy`(): List<DynamicTest> {
        val section = spec.obj("malformed")
        return section.objects("cases").map { case ->
            val input = case.string("input")
            dynamicTest(input.escaped()) {
                assertSoftly {
                    Cpf.normalize(input) shouldBe case.string("normalized")
                    Cpf.format(input) shouldBe input
                    Cpf.mask(input).shouldBeNull()
                    Cpf.formatAsYouType(input) shouldBe case.string("asYouType")
                    Cpf.validate(input).errorCode() shouldBe section.string("errorCode")
                }
            }
        }
    }

    @TestFactory
    fun `formats and masks a bad checksum without claiming validity`(): List<DynamicTest> {
        val section = spec.obj("badChecksum")
        return section.objects("cases").map { case ->
            val input = case.string("input")
            dynamicTest(input) {
                assertSoftly {
                    Cpf.format(input) shouldBe case.string("formatted")
                    Cpf.normalize(input) shouldBe case.string("normalized")
                    Cpf.mask(input) shouldBe case.string("masked")
                    Cpf.validate(input).errorCode() shouldBe section.string("errorCode")
                }
            }
        }
    }

    @TestFactory
    fun `inserts separators as digits are typed`(): List<DynamicTest> =
        spec.obj("formatAsYouType").objects("cases").map { case ->
            dynamicTest(case.string("input")) {
                Cpf.formatAsYouType(case.string("input")) shouldBe case.string("expected")
            }
        }

    @TestFactory
    fun `masks with each mode`(): List<DynamicTest> =
        spec.obj("mask").objects("modes").map { case ->
            val mode = CpfMaskMode.fromValue(case.string("mode"))
            dynamicTest("$mode ${case.string("input")}") {
                Cpf.mask(case.string("input"), mode) shouldBe case.string("expected")
            }
        }

    @TestFactory
    fun `masks with a custom character`(): List<DynamicTest> =
        spec.obj("mask").objects("customChar").map { case ->
            val mode = CpfMaskMode.fromValue(case.string("mode"))
            dynamicTest("$mode ${case.string("char")}") {
                Cpf.mask(case.string("input"), mode, case.string("char")) shouldBe case.string("expected")
            }
        }

    @TestFactory
    fun `rejects unknown mask modes`(): List<DynamicTest> =
        spec.obj("mask").strings("invalidModes").map { mode ->
            dynamicTest("\"$mode\"") {
                shouldThrow<IllegalArgumentException> { CpfMaskMode.fromValue(mode) }
            }
        }

    @TestFactory
    fun `rejects invalid mask characters`(): List<DynamicTest> {
        val mask = spec.obj("mask")
        val strings = mask.obj("invalidChars").strings("cases")
        val loneSurrogates =
            mask.obj("invalidCharsUtf16").array("cases").map { units ->
                String(units.jsonArray.map { it.jsonPrimitive.int.toChar() }.toCharArray())
            }
        return (strings + loneSurrogates).map { char ->
            dynamicTest(char.escaped()) {
                val error = shouldThrow<IllegalArgumentException> { Cpf.mask("29650899006", char = char) }
                error.message shouldBe
                    "Expected CPF mask char to be one visible Unicode code point other than a number, mark, " +
                    "or separator, but received \"$char\""
            }
        }
    }

    private fun ValidationResult<CpfException>.errorCode(): String =
        shouldBeInstanceOf<ValidationResult.Invalid<CpfException>>().error.code.name

    private fun String.escaped(): String =
        if (isEmpty()) {
            "(empty)"
        } else {
            buildString {
                for (char in this@escaped) {
                    if (char.code in 0x21..0x7E) append(char) else append("\\u%04x".format(char.code))
                }
            }
        }
}
