# 0002: Maven build and verification

Status: Implemented and verified on Eclipse Temurin 26.0.2.1 and Maven 3.9.9.

## Context

The backend uses JDK 26 and Maven 3.9.9. The initial grid checks used a standalone
runner so development could begin before the build was configured.

The team needs a shared way to compile, test, generate JavaDocs, and report
coverage without installing Maven separately on every machine.

## Decision

- Keep one Maven project in `backend/`, using the existing standard source layout.
- Generate Apache Maven Wrapper 3.3.4 with the official wrapper plugin. Pin Maven
  3.9.9 and its distribution SHA-256 in `.mvn/wrapper/maven-wrapper.properties`.
- Require JDK 26 and Maven 3.9.9 using Maven Enforcer. Compile with release 26,
  compiler warnings treated as errors, and no preview flags.
- Use JUnit Jupiter 6.1.3 for tests and JaCoCo 0.8.16 for coverage. Pin all build
  plugin versions in `pom.xml` to make the shared build reproducible.
- Bind coverage reporting and strict JavaDoc generation to `verify`. Do not add
  an arbitrary coverage threshold; assertions and requirement coverage matter.
- Keep `verify.ps1` as a convenience that delegates to Maven. It contains no
  separate compilation or test logic.
- Include `src/test/` in version control alongside production code. Keep the
  suite focused on the implemented contracts, and fail verification if no tests
  are discovered. Generated test results and coverage reports remain ignored.
- Keep Java selection in the user's environment/IDE. Do not hardcode a local JDK
  path in the build or change the system-wide Java default.

## Alternatives and consequences

Maven provides a conventional lifecycle sufficient for the current single Java
module. Gradle's flexible task model is not needed for this build. A custom script
avoids dependency setup initially, but duplicates standard test and reporting
features as the project grows. The script is now only a shortcut to the standard build.

The first wrapper run needs network access for Maven and dependencies. Later
builds reuse cached artifacts. Generated files belong under `target/` and are
ignored by Git. The earlier `build/` directory is also ignored.

Maven 3.9.9 prints warnings from some bundled libraries on JDK 26. The full build
passes with those warnings. Keep this limitation visible when evaluating a
future Maven upgrade; do not add broad warning-suppression flags.

The grid remains plain Java with no runtime dependencies. This step does not
add Spring Boot, a server entry point, CI, or pathfinding algorithms. Revisit
dependency choices as those features are implemented.

## Verification

The generated wrapper selected Maven 3.9.9 and Eclipse Temurin 26.0.2.1.
`clean verify` compiled both production and test code for Java 26, passed nine
JUnit tests, packaged the domain JAR, and generated coverage and strict JavaDocs.
See the backend README for repeatable commands and report locations.
