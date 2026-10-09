package dev.brutils

/** Keeps only the ASCII digits `0`-`9`; other Unicode digits (fullwidth, Arabic-Indic, ...) are dropped. */
internal fun String.asciiDigits(): String = filter { it in '0'..'9' }

/**
 * Joins consecutive segments of [chars] with [separators], adding a separator only when the following segment has
 * at least one character. `separators[i]` goes between segment `i` and `i + 1`.
 */
internal fun formatProgressive(
    chars: String,
    segmentLengths: IntArray,
    separators: String,
): String =
    buildString {
        var cursor = 0
        for ((index, length) in segmentLengths.withIndex()) {
            if (cursor >= chars.length) break
            if (index > 0) append(separators[index - 1])
            append(chars, cursor, minOf(cursor + length, chars.length))
            cursor += length
        }
    }

/**
 * Whether [value] is exactly one visible Unicode code point usable as a mask character: not a number, whitespace,
 * mark, control/format/private-use/surrogate/unassigned character, default-ignorable code point, or the braille
 * blank (U+2800). Modules forbid their own separators on top of this.
 *
 * Categories come from the running JDK's Unicode tables, so a character assigned in a newer Unicode version than the
 * JDK knows is treated as unassigned and rejected.
 */
internal fun isMaskCodePoint(value: String): Boolean {
    if (value.isEmpty() || value.codePointCount(0, value.length) != 1) return false
    val codePoint = value.codePointAt(0)
    return Character.getType(codePoint) !in forbiddenMaskTypes &&
        defaultIgnorable.none { codePoint in it } &&
        codePoint != BRAILLE_BLANK
}

private const val BRAILLE_BLANK = 0x2800

private val forbiddenMaskTypes: Set<Int> =
    setOf(
        // \p{N}
        Character.DECIMAL_DIGIT_NUMBER,
        Character.LETTER_NUMBER,
        Character.OTHER_NUMBER,
        // \p{M}
        Character.NON_SPACING_MARK,
        Character.ENCLOSING_MARK,
        Character.COMBINING_SPACING_MARK,
        // \p{C}
        Character.CONTROL,
        Character.FORMAT,
        Character.PRIVATE_USE,
        Character.SURROGATE,
        Character.UNASSIGNED,
        // \p{White_Space} outside \p{C}: every Zs, Zl and Zp character is White_Space and vice versa.
        Character.SPACE_SEPARATOR,
        Character.LINE_SEPARATOR,
        Character.PARAGRAPH_SEPARATOR,
    ).mapTo(HashSet()) { it.toInt() }

/** Default_Ignorable_Code_Point from Unicode's DerivedCoreProperties.txt, which java.lang.Character does not expose. */
private val defaultIgnorable: List<IntRange> =
    listOf(
        0x00AD..0x00AD,
        0x034F..0x034F,
        0x061C..0x061C,
        0x115F..0x1160,
        0x17B4..0x17B5,
        0x180B..0x180F,
        0x200B..0x200F,
        0x202A..0x202E,
        0x2060..0x206F,
        0x3164..0x3164,
        0xFE00..0xFE0F,
        0xFEFF..0xFEFF,
        0xFFA0..0xFFA0,
        0xFFF0..0xFFF8,
        0x1BCA0..0x1BCA3,
        0x1D173..0x1D17A,
        0xE0000..0xE0FFF,
    )
