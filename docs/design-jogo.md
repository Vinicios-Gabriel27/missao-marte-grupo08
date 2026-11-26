# Design do jogo — Missão Marte: Sobrevivência

Evolução do Missão Marte para um jogo de sobrevivência em 3D (estilo Vampire Survivors / Megabonk),
mantendo o núcleo do jogo original: **resgatar passageiros e voltar à plataforma**.
Mudança de gênero autorizada pelo professor.

## Visão geral

- **Visual:** 3D low-poly, câmera de cima na diagonal seguindo a nave.
- **Controle:** a nave anda livre (WASD/setas no desktop, joystick virtual no Android). **Tiro automático** no inimigo mais próximo.
- **Ciclo:** inimigos chegam em ondas crescentes → morrem e soltam cristais de XP → XP enche a barra → subir de nível pausa o jogo e oferece **3 upgrades** para escolher.

## Condição de vitória (os dois juntos)

1. **Sobreviver no mínimo 5 minutos** e
2. **resgatar todos os passageiros** da partida.

Cumpridas as duas, a **plataforma de pouso (0,0) abre** e o jogador precisa alcançá-la no meio da horda. Ao abrir, surge um **chefe**.

**Derrota:** vida da nave chega a 0.

## Passageiros (bônus acumulam, máximo de 5 a bordo)

| Passageiro | Bônus ao resgatar (por unidade) |
|---|---|
| 🎓 Professor | +25% de XP ganho |
| 🔧 Engenheiro | +1 de vida regenerada por segundo |
| 🧑‍🚀 Astronauta | +20% de dano |

- Resgate: encostar a nave no passageiro (sem tecla).
- A partida tem **5 passageiros**. Eles aparecem **ao longo do tempo** (um a cada ~50 s), em pontos aleatórios do mapa, com seta no HUD indicando a direção.
- Com a nave cheia (5), não há como resgatar mais — o mesmo limite de capacidade do jogo original.

## Inimigos

| Inimigo | Vida | Velocidade | Dano ao encostar | XP | Surge a partir de |
|---|---|---|---|---|---|
| Perseguidor (básico) | 10 | média | 10 | 1 | início |
| Rápido | 4 | alta | 5 | 1 | 1 min |
| Tanque | 60 | baixa | 25 | 5 | 2 min |
| Chefe | 800 | baixa | 40 | 50 | plataforma aberta |

As ondas aumentam a quantidade e a frequência a cada 30 s.

## Upgrades ao subir de nível (3 opções sorteadas)

**Atributos:** dano +15% · cadência de tiro +15% · velocidade +10% · vida máxima +20 · ímã de XP +30% de alcance.

**Armas:**
- **Tiro múltiplo:** níveis 1→3 = 2, 3 e 5 projéteis em leque.
- **Orbital:** níveis 1→3 = 1, 2 e 3 esferas girando em volta da nave.
- **Onda de choque:** pulso em área ao redor da nave; cada nível reduz o intervalo e aumenta o raio.

## Dificuldade (reaproveita o enum do jogo original)

| | Fácil | Médio | Difícil |
|---|---|---|---|
| Vida inicial da nave | 150 | 100 | 75 |
| Multiplicador de inimigos | 0,7× | 1× | 1,4× |

## Pontuação (vai para o ranking)

`inimigos derrotados × 10 + segundos sobrevividos × 2 + passageiros resgatados × 100 + 500 por vitória`

## Como isso se encaixa na arquitetura

- **Domínio sem libGDX:** `PartidaSobrevivencia.atualizar(delta, entrada)` avança o mundo; a tela 3D só desenha o estado e reage aos eventos (som, partículas). Testes rodam sem janela.
- **Passageiros polimórficos (OCP):** cada subclasse de `Passageiro` aplica o próprio efeito em `aplicarBonus(atributosNave)`. Novo passageiro = nova classe.
- **Armas e upgrades como Strategy:** cada arma implementa a mesma interface (`atualizar`, `subirNivel`); o sorteio de upgrades não conhece as armas concretas.
- **Inimigos por tipo de dados:** os números da tabela ficam em uma enumeração/configuração; um novo inimigo não altera o laço do jogo.

## Fora do escopo (se sobrar tempo)

Escolha de skin da nave, mais armas, efeitos visuais extras.
