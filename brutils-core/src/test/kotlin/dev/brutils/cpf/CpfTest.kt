package dev.brutils.cpf

import dev.brutils.ValidationResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

/** JVM-specific CPF behavior. Shared behavior lives in brutils-spec and runs in [CpfSpecTest]. */
class CpfTest {
    @Test
    fun `validate reports null as INVALID_TYPE`() {
        val error = Cpf.validate(null).shouldBeInstanceOf<ValidationResult.Invalid<CpfException>>().error

        error.code shouldBe CpfErrorCode.INVALID_TYPE
        error.message shouldBe "Expected a string for CPF validation, but received null."
    }

    @Test
    fun `validate exposes the result as a boolean`() {
        Cpf.validate("52263944621").isValid shouldBe true
        Cpf.validate("52263944620").isValid shouldBe false
    }

    @Test
    fun `mask validates the character before the input shape`() {
        shouldThrow<IllegalArgumentException> { Cpf.mask("12", char = "0") }
        Cpf.mask("12", CpfMaskMode.REDACTED) shouldBe null
    }

    @Test
    fun `fromValue names every mode in its error`() {
        shouldThrow<IllegalArgumentException> { CpfMaskMode.fromValue("redact") }.message shouldBe
            "Expected CPF mask mode to be one of \"suffix\", \"prefix-suffix\", \"redacted\", but received \"redact\""
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
    fun `generate rerolls a base of one repeated digit`(digit: Int) {
        val replacement = (digit + 1) % 10
        // Nine repeated digits, then the reroll index 0, a draw equal to the repeated digit, and the replacement.
        val random = SequenceRandom(List(9) { digit } + listOf(0, digit, replacement))

        val generated = Cpf.generate(random = random)

        generated.take(9) shouldBe "$replacement" + "$digit".repeat(8)
        Cpf.validate(generated) shouldBe ValidationResult.Valid
        random.calls shouldBe 12
    }

    @Test
    fun `generate always returns a valid CPF`() {
        val random = Random(seed = 2026)
        repeat(1_000) {
            Cpf.validate(Cpf.generate(random = random)) shouldBe ValidationResult.Valid
            Cpf.validate(Cpf.generate(formatted = true, random = random)) shouldBe ValidationResult.Valid
        }
    }

    @Test
    fun `CpfException uses the code as its default message`() {
        val error = CpfException(CpfErrorCode.REPEATED_DIGITS)

        error.message shouldBe "REPEATED_DIGITS"
        error.shouldBeInstanceOf<IllegalArgumentException>()
        CpfException(CpfErrorCode.REPEATED_DIGITS, "Custom message").message shouldBe "Custom message"
    }

    @Test
    fun `patterns match exactly the accepted shapes`() {
        Regex(Cpf.RAW_PATTERN).matches("52263944621") shouldBe true
        Regex(Cpf.FORMATTED_PATTERN).matches("522.639.446-21") shouldBe true
        Regex(Cpf.FORMATTED_PATTERN).matches("522.639.446-21\n") shouldBe false
        Cpf.LENGTH shouldBe 11
    }
}
