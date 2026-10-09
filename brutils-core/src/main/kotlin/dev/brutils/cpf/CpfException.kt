package dev.brutils.cpf

/** Why a CPF failed validation. */
public enum class CpfErrorCode {
    /** The value is `null`. */
    INVALID_TYPE,

    /** The value is neither 11 ASCII digits nor the canonical `XXX.XXX.XXX-XX` shape. */
    INVALID_FORMAT,

    /** All 11 digits are the same, which the checksum alone would accept. */
    REPEATED_DIGITS,

    /** A check digit does not match. */
    INVALID_CHECKSUM,
}

/**
 * A CPF validation failure, carrying a machine-readable [code]. Returned inside
 * [dev.brutils.ValidationResult.Invalid] by [Cpf.validate] rather than thrown.
 */
public class CpfException
    @JvmOverloads
    constructor(
        public val code: CpfErrorCode,
        message: String = code.name,
    ) : IllegalArgumentException(message)
