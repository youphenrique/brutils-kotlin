# brutils-kotlin

Guidelines for AI coding agents and human contributors working on `brutils-kotlin`, the JVM implementation of
[brutils](https://github.com/youphenrique/brutils-js).

---

## Project Overview

A Gradle multi-module library published under the `dev.brutils` group. It mirrors the behavior of `@brutils/core`,
not its API shape: the API is idiomatic Kotlin and must read naturally from Java.

### Key Technologies

- **Build:** Gradle (Kotlin DSL) with convention plugins in `build-logic/` and versions in `gradle/libs.versions.toml`.
- **Language:** Kotlin with `explicitApi()`, compiled for Java 17 (`-Xjdk-release=17`) whatever JDK runs the build.
- **Testing:** JUnit 6 with Kotest assertions.
- **Lint:** ktlint (`ktlint_official` style).
- **API stability:** Kotlin ABI validation; dumps live in `<module>/api/`.
- **Docs:** Dokka.
- **Publishing:** `com.vanniktech.maven.publish` to the Maven Central Portal.

---

## Project Structure

```
build-logic/          # Convention plugins: brutils.kotlin-library, brutils.publishing, brutils.spec
brutils-core/         # Utilities; no dependencies beyond the Kotlin stdlib
brutils-jakarta/      # Jakarta Validation constraints built on brutils-core
brutils-bom/          # Java platform aligning module versions
```

Packages follow `dev.brutils.<module>` (for example `dev.brutils.cpf`). Each library module's
`Automatic-Module-Name` is `dev.brutils.<module name without the brutils- prefix>`.

---

## Command Reference

| Task                                  | Command                                              |
| :------------------------------------ | :--------------------------------------------------- |
| Lint, ABI check, and test everything  | `./gradlew build`                                    |
| Test against a local spec checkout    | `./gradlew build -Pbrutils.spec.dir=../brutils-spec` |
| Run one module's tests                | `./gradlew :brutils-core:test`                       |
| Fix formatting                        | `./gradlew ktlintFormat`                             |
| Accept an intended public API change  | `./gradlew updateKotlinAbi`                          |
| Generate API docs                     | `./gradlew dokkaGenerate`                            |
| Coverage report                       | `./gradlew koverHtmlReport`                          |
| Install locally for the ERP to use    | `./gradlew publishToMavenLocal`                      |

---

## Key Conventions

### 1. The spec is the source of truth

Behavior is defined by the language-neutral cases in [brutils-spec](https://github.com/youphenrique/brutils-spec),
synced into `build/brutils-spec` by the root `syncSpec` task and loaded in tests through `dev.brutils.spec.Spec`. A
behavior change starts in the spec; this repository then bumps `brutils.spec.ref` to the new commit SHA and makes the
tests pass.
Kotlin-only concerns (Java interop, nullability, exception types) are tested here.

### 2. Java interop is part of the API

- Top-level functions get `@file:JvmName` so Java calls `Cpf.validate(...)`, not `CpfKt.validate(...)`.
- Use `@JvmStatic` and `@JvmOverloads` where Java callers would otherwise lose defaults or need `.INSTANCE`.
- Do not expose `kotlin.Result`, value classes, or suspend functions in the public API.
- Validation results are a `sealed interface` with success and failure cases, usable from Java 21 pattern matching.
- Error codes are enums; errors are exceptions carrying the code, mirroring the TS module's `code` property.
- Cover each public entry point with at least one test written in Java (`src/test/java`).

### 3. No platform APIs in core logic

`brutils-core` uses only the Kotlin stdlib (no `java.*` in main sources) so it can move to Kotlin Multiplatform later.

### 4. Public API changes are deliberate

`explicitApi()` requires visibility on every declaration, and `checkKotlinAbi` fails the build when the public API
drifts from `<module>/api/*.api`. Run `updateKotlinAbi` only for intended changes and commit the dump with them.

---

## Verify Before Committing

Run `./gradlew build`. It lints, checks the ABI, and runs the tests.
