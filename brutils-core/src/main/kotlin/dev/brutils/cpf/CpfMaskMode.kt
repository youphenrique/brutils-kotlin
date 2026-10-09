package dev.brutils.cpf

/** Which CPF digits [Cpf.mask] reveals. */
public enum class CpfMaskMode(
    /** The mode's name in the brutils spec and the JS implementation, such as `"prefix-suffix"`. */
    public val value: String,
) {
    /** Reveals the last 2 digits: `***.***.***-21`. */
    SUFFIX("suffix"),

    /** Reveals the first 3 and last 2 digits: `522.***.***-21`. */
    PREFIX_SUFFIX("prefix-suffix"),

    /** Reveals nothing: `***.***.***-**`. */
    REDACTED("redacted"),
    ;

    public companion object {
        /**
         * Returns the mode whose [value] is [value], for modes read from configuration or other implementations.
         *
         * @throws IllegalArgumentException If no mode has that value.
         */
        @JvmStatic
        public fun fromValue(value: String): CpfMaskMode =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException(
                    "Expected CPF mask mode to be one of ${entries.joinToString { "\"${it.value}\"" }}, " +
                        "but received \"$value\"",
                )
    }
}
