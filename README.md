# brutils-kotlin

Brazilian utilities for **Kotlin and Java 17+**: validation, formatting, and generation of CPF, CNPJ (including the
alphanumeric format), CEP, and more.

The JVM sibling of [brutils](https://github.com/youphenrique/brutils-js) (TypeScript). Both implementations run the
same language-neutral conformance cases, so a CPF that is valid in one is valid in the other.

> **Status:** pre-release. CPF is implemented; nothing is published to Maven Central yet.

## Modules

| Artifact                      | Purpose                                                              |
| :---------------------------- | :------------------------------------------------------------------- |
| `dev.brutils:brutils-core`    | Validation, formatting, and generation. No dependencies beyond the Kotlin stdlib. |
| `dev.brutils:brutils-jakarta` | Jakarta Validation constraints (Spring Boot, Quarkus, Micronaut).    |
| `dev.brutils:brutils-bom`     | Aligns the versions of all modules.                                  |

## Installation

```kotlin
dependencies {
    implementation(platform("dev.brutils:brutils-bom:<version>"))
    implementation("dev.brutils:brutils-core")
}
```

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>dev.brutils</groupId>
      <artifactId>brutils-bom</artifactId>
      <version><!-- version --></version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Usage

```kotlin
import dev.brutils.ValidationResult
import dev.brutils.cpf.Cpf
import dev.brutils.cpf.CpfMaskMode

when (val result = Cpf.validate("522.639.446-21")) {
    ValidationResult.Valid -> println("valid")
    is ValidationResult.Invalid -> println(result.error.code) // INVALID_FORMAT, REPEATED_DIGITS, ...
}

Cpf.format("52263944621") // "522.639.446-21"
Cpf.formatAsYouType("5226") // "522.6"
Cpf.mask("52263944621", CpfMaskMode.PREFIX_SUFFIX) // "522.***.***-21"
Cpf.generate(formatted = true) // a random valid CPF, for test fixtures
```

The same calls work from Java:

```java
ValidationResult<CpfException> result = Cpf.validate("522.639.446-21");
if (result instanceof ValidationResult.Invalid<CpfException> invalid) {
    System.out.println(invalid.error().getCode());
}

Cpf.mask("52263944621", CpfMaskMode.REDACTED, "#"); // "###.###.###-##"
```

## Compatibility

- **Java 17 or newer**, the floor of Spring Boot 4, Quarkus 3, and Micronaut 4. CI runs on JDK 17, 21, and 25.
- Designed to be called from Java as naturally as from Kotlin.
- Follows semantic versioning; the public binary API is checked on every build.

## Development

```bash
./gradlew build
```

Tests run against the conformance cases in [brutils-spec](https://github.com/youphenrique/brutils-spec), pinned by
`brutils.spec.ref` in `gradle.properties`. To test against a local checkout instead:

```bash
./gradlew build -Pbrutils.spec.dir=../brutils-spec
```

See [AGENTS.md](AGENTS.md) for conventions.

## License

[MIT](LICENSE)
