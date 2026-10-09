package dev.brutils.cpf

import dev.brutils.ValidationResult
import dev.brutils.asciiDigits
import dev.brutils.formatProgressive
import dev.brutils.isMaskCodePoint
import kotlin.random.Random

/**
 * CPF (Cadastro de Pessoas Físicas): validation, formatting, masking, and generation.
 *
 * Only ASCII digits count as digits. The two accepted shapes are 11 raw digits (`52263944621`) and the canonical
 * mask (`522.639.446-21`).
 */
public object Cpf {
    /** Number of digits in a CPF. */
    public const val LENGTH: Int = 11

    /** Pattern matching exactly 11 ASCII digits. A compile-time constant, so it works in annotations such as `@Pattern`. */
    public const val RAW_PATTERN: String = "[0-9]{11}"

    /** Pattern matching exactly the canonical `XXX.XXX.XXX-XX` shape. */
    public const val FORMATTED_PATTERN: String = "[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}"

    private val rawRegex = Regex(RAW_PATTERN)
    private val formattedRegex = Regex(FORMATTED_PATTERN)
    private val segmentLengths = intArrayOf(3, 3, 3, 2)

    /**
     * Extracts all ASCII digits from [value] without validating it. This is lossy: malformed input can produce the
     * same result as a valid CPF.
     *
     * ```kotlin
     * Cpf.normalize("779.333.21") // "77933321"
     * Cpf.normalize("779.333.210-54") // "77933321054"
     * Cpf.normalize("779.333.210-5466") // "7793332105466"
     * ```
     *
     * @return All extracted digits, including those from partial or overlong input.
     */
    @JvmStatic
    public fun normalize(value: String): String = value.asciiDigits()

    /**
     * Formats exactly 11 ASCII digits as `XXX.XXX.XXX-XX`. Every other string, including canonical formatted input,
     * is returned unchanged. Formatting does not check validity or add digits.
     *
     * ```kotlin
     * Cpf.format("52263944621") // "522.639.446-21"
     * Cpf.format("522.639.446-21") // "522.639.446-21"
     * Cpf.format("123") // "123"
     * ```
     */
    @JvmStatic
    public fun format(value: String): String {
        if (!rawRegex.matches(value)) return value
        return "${value.substring(0, 3)}.${value.substring(3, 6)}.${value.substring(6, 9)}-${value.substring(9)}"
    }

    /**
     * Displays an editable CPF field using the pattern `XXX.XXX.XXX-XX`. Keeps only ASCII digits, takes the first
     * 11, and inserts each separator only when the following segment has a digit. This is lossy UI behavior, not
     * parsing or validation: validate the submitted value, not this output.
     *
     * ```kotlin
     * Cpf.formatAsYouType("5226") // "522.6"
     * Cpf.formatAsYouType("5226394462") // "522.639.446-2"
     * Cpf.formatAsYouType("abc522639446219") // "522.639.446-21" (lossy)
     * ```
     */
    @JvmStatic
    public fun formatAsYouType(value: String): String =
        formatProgressive(normalize(value).take(LENGTH), segmentLengths, "..-")

    /**
     * Masks a complete CPF for display. Accepts 11 ASCII digits or the canonical shape without checking the
     * checksum. This is display masking, not anonymization.
     *
     * ```kotlin
     * Cpf.mask("52263944621") // "***.***.***-21"
     * Cpf.mask("52263944621", CpfMaskMode.PREFIX_SUFFIX) // "522.***.***-21"
     * Cpf.mask("52263944621", CpfMaskMode.REDACTED, char = "#") // "###.###.###-##"
     * Cpf.mask("5226") // null
     * ```
     *
     * @param mode Which digits stay visible.
     * @param char Replaces each hidden digit. Must be exactly one visible Unicode code point (checked by code point,
     *   not grapheme) other than a number, whitespace, mark, control or format character, `.`, or `-`.
     * @return The masked CPF, or `null` for any other shape. Show a neutral placeholder for `null`, never the
     *   original value.
     * @throws IllegalArgumentException If [char] is not allowed, even when [value] is malformed.
     */
    @JvmStatic
    @JvmOverloads
    public fun mask(
        value: String,
        mode: CpfMaskMode = CpfMaskMode.SUFFIX,
        char: String = "*",
    ): String? {
        require(isMaskCodePoint(char) && char != "." && char != "-") {
            "Expected CPF mask char to be one visible Unicode code point other than a number, mark, or separator, " +
                "but received \"$char\""
        }
        if (!rawRegex.matches(value) && !formattedRegex.matches(value)) return null

        val hiddenGroup = char.repeat(3)
        val prefix = if (mode == CpfMaskMode.PREFIX_SUFFIX) value.substring(0, 3) else hiddenGroup
        val suffix = if (mode == CpfMaskMode.REDACTED) char.repeat(2) else value.takeLast(2)
        return "$prefix.$hiddenGroup.$hiddenGroup-$suffix"
    }

    /**
     * Generates a random CPF with valid check digits, for test fixtures. Generated values can coincide with real,
     * assigned CPFs; never use one as an identity or a secret.
     *
     * ```kotlin
     * Cpf.generate() // "12345678909"
     * Cpf.generate(formatted = true) // "123.456.789-09"
     * ```
     *
     * @param formatted Whether to return the canonical `XXX.XXX.XXX-XX` shape.
     * @param random Source of the 9 base digits. The default is not cryptographically secure; pass a seeded instance
     *   for reproducible fixtures.
     */
    @JvmStatic
    @JvmOverloads
    public fun generate(
        formatted: Boolean = false,
        random: Random = Random.Default,
    ): String {
        val digits = IntArray(LENGTH)
        for (index in 0 until 9) digits[index] = random.nextInt(10)

        // A base of one repeated digit always yields a repeated-digit CPF, which validate rejects.
        if ((1 until 9).all { digits[it] == digits[0] }) {
            val rerollIndex = random.nextInt(9)
            var rerolled = random.nextInt(10)
            while (rerolled == digits[0]) rerolled = random.nextInt(10)
            digits[rerollIndex] = rerolled
        }

        digits[9] = checkDigit(digits, 9)
        digits[10] = checkDigit(digits, 10)

        val raw = digits.joinToString("")
        return if (formatted) format(raw) else raw
    }

    /**
     * Validates exactly 11 ASCII digits or the canonical `XXX.XXX.XXX-XX` shape. Other strings are
     * [CpfErrorCode.INVALID_FORMAT]; well-shaped values are then checked for repeated digits and check digits.
     * This does not prove issuance or check the CPF's status at the Receita Federal.
     *
     * ```kotlin
     * Cpf.validate("522.639.446-21") // Valid
     * Cpf.validate("123") // Invalid(CpfException(INVALID_FORMAT))
     * ```
     *
     * @return [ValidationResult.Valid], or [ValidationResult.Invalid] with the first failing rule in this order:
     *   [CpfErrorCode.INVALID_TYPE] (`null`), [CpfErrorCode.INVALID_FORMAT], [CpfErrorCode.REPEATED_DIGITS],
     *   [CpfErrorCode.INVALID_CHECKSUM].
     */
    @JvmStatic
    public fun validate(value: String?): ValidationResult<CpfException> {
        val error = findError(value) ?: return ValidationResult.Valid
        return ValidationResult.Invalid(error)
    }

    private fun findError(value: String?): CpfException? {
        if (value == null) {
            return CpfException(CpfErrorCode.INVALID_TYPE, "Expected a string for CPF validation, but received null.")
        }
        if (!rawRegex.matches(value) && !formattedRegex.matches(value)) {
            return CpfException(
                CpfErrorCode.INVALID_FORMAT,
                "Only 11 digits or ###.###.###-## format are accepted.",
            )
        }

        val digits = value.asciiDigits().map { it - '0' }.toIntArray()
        if (digits.all { it == digits[0] }) {
            return CpfException(CpfErrorCode.REPEATED_DIGITS, "CPF cannot contain all identical digits.")
        }
        if (checkDigit(digits, 9) != digits[9] || checkDigit(digits, 10) != digits[10]) {
            return CpfException(CpfErrorCode.INVALID_CHECKSUM, "Invalid CPF check digits.")
        }
        return null
    }

    /** Mod-11 check digit over the first [count] digits, weighted from `count + 1` down to 2. */
    private fun checkDigit(
        digits: IntArray,
        count: Int,
    ): Int {
        var sum = 0
        for (index in 0 until count) sum += digits[index] * (count + 1 - index)
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }
}
