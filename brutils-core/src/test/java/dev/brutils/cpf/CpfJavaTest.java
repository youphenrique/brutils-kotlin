package dev.brutils.cpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.brutils.ValidationResult;
import java.util.Random;
import kotlin.random.PlatformRandomKt;
import org.junit.jupiter.api.Test;

/** Checks that the CPF API reads naturally from Java 17. */
class CpfJavaTest {
    @Test
    void validatesWithResultTypes() {
        assertSame(ValidationResult.Valid.INSTANCE, Cpf.validate("522.639.446-21"));
        assertTrue(Cpf.validate("52263944621").isValid());

        ValidationResult<CpfException> result = Cpf.validate("123");
        ValidationResult.Invalid<CpfException> invalid = assertInstanceOf(ValidationResult.Invalid.class, result);
        assertFalse(invalid.isValid());
        assertEquals(CpfErrorCode.INVALID_FORMAT, invalid.error().getCode());
    }

    @Test
    void treatsNullAsInvalidTypeOnlyInValidate() {
        ValidationResult<CpfException> result = Cpf.validate(null);
        if (!(result instanceof ValidationResult.Invalid<CpfException> invalid)) {
            throw new AssertionError("Expected an invalid result, got " + result);
        }
        assertEquals(CpfErrorCode.INVALID_TYPE, invalid.error().getCode());

        assertThrows(NullPointerException.class, () -> Cpf.format(null));
    }

    @Test
    void transformsWithoutKotlinDefaults() {
        assertEquals("52263944621", Cpf.normalize("522.639.446-21"));
        assertEquals("522.639.446-21", Cpf.format("52263944621"));
        assertEquals("522.6", Cpf.formatAsYouType("5226"));
        assertEquals("***.***.***-21", Cpf.mask("52263944621"));
        assertEquals("522.***.***-21", Cpf.mask("52263944621", CpfMaskMode.PREFIX_SUFFIX));
        assertEquals("###.###.###-##", Cpf.mask("52263944621", CpfMaskMode.REDACTED, "#"));
        assertNull(Cpf.mask("5226"));
        assertSame(CpfMaskMode.PREFIX_SUFFIX, CpfMaskMode.fromValue("prefix-suffix"));
        assertEquals(11, Cpf.LENGTH);
    }

    @Test
    void generatesWithOverloadsAndAJavaRandom() {
        assertTrue(Cpf.validate(Cpf.generate()).isValid());
        assertTrue(Cpf.validate(Cpf.generate(true)).isValid());

        String first = Cpf.generate(false, PlatformRandomKt.asKotlinRandom(new Random(42)));
        String second = Cpf.generate(false, PlatformRandomKt.asKotlinRandom(new Random(42)));
        assertEquals(first, second);
    }
}
