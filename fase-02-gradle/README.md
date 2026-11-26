# Fase 2 — Gradle e testes

Código da Fase 1 migrado para o layout Gradle (`src/main/java`, `src/test/java`) com Gradle Wrapper 8.7 e JUnit 5.

```bash
cd fase-02-gradle
./gradlew test               # Windows: .\gradlew.bat test
./gradlew run --console=plain
./gradlew build              # compila + testes + jar
```

Relatório dos testes: `build/reports/tests/test/index.html`.
