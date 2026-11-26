# Fase 7 — Evolução livre: raridade, Investida e chefe com padrões de ataque

Melhorias inspiradas em **Megabonk** e **The Deep Horde**, integradas à arquitetura existente e cobertas
por testes (`cliente/core/src/test/.../EvolucaoLivreTest.java`). Base: Fase 6 (desktop + Android + servidor).

## O que mudou

### 1. Raridade dos upgrades (Megabonk)
Cada opção de nível vem com uma raridade sorteada por peso:

| Raridade | Chance | Efeito em atributos | Efeito em armas |
|---|---|---|---|
| Comum | 60% | ×1 | +1 nível |
| Raro | 27% | ×1,5 | +1 nível |
| Épico | 10% | ×2 | **+2 níveis** |
| Lendário | 3% | ×3 | **+2 níveis** |

Os cartões mostram a cor da raridade e o valor real ("Dano +45%").
Código: `upgrades/Raridade`, `upgrades/OpcaoUpgrade` (upgrade + raridade), `Upgrade.aplicar(partida, raridade)`.

### 2. Poder especial: Investida (The Deep Horde)
Espaço/Shift (celular: botão **DASH**). A nave arranca 7 unidades na direção em que está virada,
**atravessa a horda causando dano** e fica **invulnerável** durante a arrancada (+0,35 s). Recarga de 6 s,
mostrada num anel no HUD.
Código: `PoderEspecial` (interface/Strategy) e `Investida`. Outra nave poderia ter outro poder sem mudar a `Partida`.

### 3. Chefe com padrões de ataque
Máquina de estados `ComportamentoChefe`: **perseguir → mirar** (para e mostra uma faixa vermelha no chão
por onde vai passar) **→ investir → anel de projéteis → perseguir**. Abaixo de 50% de vida fica **furioso**:
mais rápido e com anel duplo (24 + 12). Os projéteis (`ProjetilInimigo`) machucam a nave — a Investida
passa por eles sem dano, o que cria uma decisão tática.

### 4. Sistema de cartas (níveis, novas cartas e cartas cruéis)
As opções de nível viraram **cartas** com ícone, moldura da raridade e **marcadores de nível (1 a 5)**.

- **Armas (5 níveis):** Tiro múltiplo, Orbital, Onda de choque, **Mísseis teleguiados** (perseguem e explodem em área),
  **Raio em cadeia** (salta entre inimigos).
- **Passivas (5 níveis):** Dano, Cadência, Velocidade, Vida máxima, Ímã, **Armadura** (bloqueia dano), **Área**
  (raios maiores), **Crítico** (dano dobrado).
- **Cartas cruéis (evoluções):** arma no nível 5 + a passiva certa → a carta aparece **garantida** entre as opções:

| Arma (nível 5) | + Passiva | Carta cruel |
|---|---|---|
| Tiro múltiplo | Cadência | **Tempestade de Plasma** — rajadas de 12 tiros em 360° |
| Orbital | Área | **Anel de Saturno** — 6 esferas gigantes, giro rápido, dano dobrado |
| Onda de choque | Armadura | **Terremoto Marciano** — ondas enormes que empurram a horda |
| Mísseis teleguiados | Crítico | **Chuva de Meteoros** — salvas de 5 mísseis com explosões enormes |
| Raio em cadeia | Velocidade | **Tempestade Elétrica** — raios a cada 0,6 s saltando por 14 inimigos |

Código: `upgrades/Evolucao` (enum: arma + passiva + regra), `armas/ArmaBase` (nível e evolução comuns),
`SorteadorUpgrades` (garante a evolução), `PartidaSobrevivencia.nivelPassiva`. Testes: `CartasTest`.
Ícones das cartas desenhados para o projeto (`assets/icones`).

## Comportamento anterior preservado

- Regras de vitória/derrota, pontuação, passageiros, ondas e ranking **não mudaram** (o servidor da Fase 5
  continua compatível sem alteração).
- Todos os testes das fases anteriores continuam passando (a assinatura `opcoesUpgrade()` agora devolve
  `OpcaoUpgrade`; o teste antigo foi ajustado só no tipo).
- Mudança intencional: o chefe não apenas persegue — documentada acima e testada.

## Como a arquitetura facilitou

- **Strategy** (`Arma`, `PoderEspecial`) e **eventos do domínio**: a tela só reage a `PODER_ATIVADO`,
  `CHEFE_MIRANDO`, `CHEFE_INVESTIU`, `CHEFE_ANEL` (som, tremor, aviso), sem regra de jogo na interface.
- **Enum com dados** (`Raridade`, `UpgradeAtributo`): balancear é mudar números, não lógica.
- **Domínio sem libGDX**: as três mecânicas foram desenvolvidas e testadas sem abrir janela.

## Executar

```bash
cd fase-07-evolucao-livre/cliente && ./gradlew :lwjgl3:run     # desktop
./gradlew test                                                 # testes do domínio
./gradlew :android:assembleDebug                               # APK (com Android SDK)
cd ../servidor && ./gradlew bootRun                            # servidor (opcional)
```
