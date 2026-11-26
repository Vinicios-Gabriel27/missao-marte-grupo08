# Fase 4 — Interface gráfica: menu 3D, fontes, sons, HUD e ranking local

Evolução da Fase 3 (o domínio `sobrevivencia` é o mesmo). Esta fase cuida da apresentação.

## Executar (JDK 17+)

```bash
cd fase-04-interface-grafica
./gradlew :lwjgl3:run      # Windows: .\gradlew.bat :lwjgl3:run
./gradlew test
```

## O que há nesta fase

| Recurso | Onde |
|---|---|
| **Menu** com fundo 3D animado (base marciana, nave, passageiros, alienígenas), botões com mouse/teclado/toque, nome do piloto, dificuldade, som | `TelaMenu`, `CenaVitrine`, `Botao` |
| **Fontes TTF** geradas em tempo de execução (gdx-freetype): Orbitron (títulos) e Rajdhani (textos) — licença OFL | `Fontes`, `assets/fontes` |
| **Sons e música** sintetizados para o projeto; sons frequentes têm limite de repetição | `Sons`, `assets/sons` |
| **HUD** em painéis: vida animada, XP, tempo e objetivo, nível, passageiros, barra do chefe, números de dano, alerta de vida baixa | `Hud`, `Ui` |
| **Cartões de upgrade** animados, com cor por tipo (atributo/arma) | `Hud` |
| **Tela final** com estatísticas, pontuação e posição no ranking | `TelaFim` |
| **Ranking local** (Top 10, arquivo JSON) | `ranking/ServicoRanking` (Java puro, testado) + `gdx/RepositorioRankingLocal` |
| **Renderização compartilhada** (PBR, sombras, névoa, luzes dinâmicas, bloom, cor, vinheta) criada uma vez | `Mundo3D`, `PosProcessamento` |

## Recursos e ciclo de vida

Recursos caros (modelos, shaders, fontes, sons) são criados uma vez em `MissaoMarteJogo.create()`
e liberados em `dispose()`. As telas só reutilizam. Cada tela desenha o 3D com `Mundo3D.desenhar(...)`
passando *o que* desenhar; a ordem das passadas (sombras → PBR → efeitos → pós-processamento) fica num lugar só.

## Ranking local (arquitetura)

`ServicoRanking` depende da interface `RepositorioRanking` (DIP). Aqui a implementação é um arquivo JSON
local; na Fase 5 o ranking global entra como outra implementação, sem mudar o serviço.

## Créditos

Modelos 3D: Kenney (CC0). Fontes: Orbitron e Rajdhani (SIL Open Font License; o til da Orbitron foi
redesenhado para o português). Sons e música: gerados por síntese para este projeto.
