# brutils-kotlin

Brazilian utilities for **Kotlin and Java 17+**: validation, formatting, and generation of CPF, CNPJ (including the
alphanumeric format), CEP, and more.

The JVM sibling of [brutils](https://github.com/youphenrique/brutils-js) (TypeScript). Both implementations run the
same language-neutral conformance cases, so a CPF that is valid in one is valid in the other.

> **Status:** scaffold. No module is published yet.

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

## Compatibility

- **Java 17 or newer**, the floor of Spring Boot 4, Quarkus 3, and Micronaut 4. CI runs on JDK 17, 21, and 25.
- Designed to be called from Java as naturally as from Kotlin.
- Follows semantic versioning; the public binary API is checked on every build.

## Development

```bash
./gradlew build
```

Tests run against the conformance cases in [`spec/`](https://github.com/youphenrique/brutils-js/tree/main/spec) of the
brutils repository, pinned by `brutils.spec.ref` in `gradle.properties`. To test against a local checkout instead:

```bash
./gradlew build -Pbrutils.spec.dir=../brutils/spec
```

See [AGENTS.md](AGENTS.md) for conventions.

## License

[MIT](LICENSE)
