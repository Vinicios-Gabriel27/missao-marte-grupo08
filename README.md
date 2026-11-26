# Missão Marte Unifor — Projeto Integrador (T200)

Evolução do jogo **Missão Marte Unifor**: de uma aplicação Java de console até um jogo gráfico multiplataforma (libGDX desktop + Android) com servidor de ranking (Spring Boot).

**Equipe:** Grupo08-2026 — _preencher nomes e usuários GitHub_
**Quadro Kanban:** _link do GitHub Project_

## Fases

| Fase | Pasta | Sprint | Situação |
|---|---|---|---|
| 0 — Projeto inicial (console) | `fase-0-console-original` | 1 | Preservado |
| 1 — Refatoração arquitetural | `fase-01-refatoracao` | 1 | Concluída |
| 2 — Gradle + JUnit 5 | `fase-02-gradle` | 1 | Concluída |
| 3 — libGDX 3D (modo sobrevivência) | `fase-03-libgdx` | 2 | Jogável |
| 4 — Interface gráfica (menu 3D, sons, HUD, ranking local) | `fase-04-interface-grafica` | 2 | Jogável |
| 5 — Servidor e ranking global (Spring Boot + cliente online) | `fase-05-servidor-ranking` | 3 | Concluída |
| 6 — Android (joystick virtual, APK no Releases) | `fase-06-android` | 4 | Concluída |
| 7 — Evolução livre (raridade, Investida, chefe com padrões) | `fase-07-evolucao-livre` | 4 | Concluída |

Cada fase é uma cópia de trabalho independente; fases anteriores não são alteradas.

## Requisitos de ambiente

- JDK 17 ou superior (`java -version`)
- Gradle não precisa estar instalado: use o wrapper (`gradlew` / `gradlew.bat`) de cada fase

## Execução rápida

```bash
cd fase-02-gradle
./gradlew test
./gradlew run --console=plain
```

Instruções específicas no README de cada fase.

## Documentação

- [Arquitetura e decisões — Sprint 1](docs/arquitetura-sprint1.md)
- [Design do jogo (modo sobrevivência)](docs/design-jogo.md)

## Segurança

Nenhuma senha, token ou chave é versionada. Configurações sensíveis (Fase 5 em diante) vêm de variáveis de ambiente.
