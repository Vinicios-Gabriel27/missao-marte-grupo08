# Fase 3 — libGDX 3D: modo sobrevivência

O Missão Marte vira um jogo de sobrevivência em 3D (estilo Vampire Survivors / Megabonk).
Design completo em [`docs/design-jogo.md`](../docs/design-jogo.md).

## Executar (JDK 17+)

```bash
cd fase-03-libgdx
./gradlew :lwjgl3:run      # Windows: .\gradlew.bat :lwjgl3:run
./gradlew test             # testes do domínio (sem abrir janela)
```

## Controles

| Tecla | Ação |
|---|---|
| WASD / setas | mover a nave |
| (automático) | atirar no inimigo mais próximo |
| 1 / 2 / 3 ou clique | escolher upgrade ao subir de nível |
| P / Esc | pausar (Q no pause volta ao menu) |

## Estrutura

```
core/
  sobrevivencia/        regras do jogo — Java puro, nenhum import da libGDX
    PartidaSobrevivencia   laço do jogo: atualizar(delta, direção) → eventos
    CampoDeBatalha         o que as armas podem ver/fazer (ISP)
    armas/                 Strategy: TiroAutomatico, Orbital, OndaDeChoque
    upgrades/              Upgrade, UpgradeAtributo, UpgradeArma, SorteadorUpgrades
    Passageiro             Professor (+XP), Engenheiro (regeneração), Astronauta (+dano)
    GeradorOndas, CronogramaPassageiros   interfaces injetadas (DIP; testes usam "nenhum")
  gdx/                  apresentação libGDX
    MissaoMarteJogo        Game: recursos 2D compartilhados e troca de telas
    TelaMenu, TelaJogo, TelaFim
    Modelos                modelos 3D low-poly gerados por código (ModelBuilder)
    Hud                    vida, XP, tempo, armas, avisos e cartões de upgrade
lwjgl3/                 janela desktop (Lwjgl3Launcher)
```

## Ciclo de vida libGDX

`create()` cria os recursos (fonte, SpriteBatch) → `render()` roda a cada quadro
(lê o controle, chama `partida.atualizar`, desenha o 3D e o HUD) → `resize()` ajusta a câmera →
`pause()` pausa a partida → `dispose()` libera modelos e batches. O domínio não participa do
ciclo de vida: só recebe o `delta` e a direção, por isso é testado sem OpenGL.

## Testes

`core/src/test`: movimento e limites da arena, perseguição e dano, tiro automático, XP e
subida de nível (pausa para escolha), ímã, bônus e capacidade dos passageiros, regeneração,
abertura da plataforma, vitória, derrota, orbital, limite de nível das armas, ondas e cronograma.
