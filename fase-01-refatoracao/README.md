# Fase 1 — Refatoração arquitetural

Mesmo jogo da Fase 0, com as regras separadas da interface de console. Detalhes em [`docs/arquitetura-sprint1.md`](../docs/arquitetura-sprint1.md).

## Executar (Java 17+)

```bash
cd fase-01-refatoracao
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out br.edu.unifor.missaomarte.Main
```

PowerShell:

```powershell
cd fase-01-refatoracao
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d out $files
java -cp out br.edu.unifor.missaomarte.Main
```
