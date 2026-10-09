package dev.brutils

/**
 * Outcome of a brutils `validate` function. Expected invalid input is reported as [Invalid] instead of thrown;
 * unexpected internal failures still propagate as exceptions.
 *
 * ```kotlin
 * when (val result = Cpf.validate(input)) {
 *     ValidationResult.Valid -> save(input)
 *     is ValidationResult.Invalid -> reject(result.error.code)
 * }
 * ```
 *
 * From Java 21, [Invalid] is a record, so `case ValidationResult.Invalid(var error) -> ...` works in a `switch`.
 *
 * @param E The module's exception type, which carries a machine-readable error code.
 */
public sealed interface ValidationResult<out E : Exception> {
    /** `true` when this is [Valid]. */
    public val isValid: Boolean

    /** The input is valid. */
    public data object Valid : ValidationResult<Nothing> {
        override val isValid: Boolean get() = true
    }

    /**
     * The input is invalid.
     *
     * @property error Why the input is invalid. It is created, not thrown; throw it if invalid input is fatal.
     */
    @JvmRecord
    public data class Invalid<out E : Exception>(
        public val error: E,
    ) : ValidationResult<E> {
        override val isValid: Boolean get() = false
    }
}
