# Arquitetura — Sprint 1 (Fases 0, 1 e 2)

## 1. Como o jogo funciona (Fase 0, comportamento de referência)

- Menu: 1 jogar, 2 ranking Top 5, 3 resetar ranking, 4 sair.
- Partida: o jogador escolhe nome, dificuldade e tamanho do mapa (de `-n` a `+n`).
- A nave começa na plataforma `L` em (0,0), com 3 vidas e capacidade para 5 passageiros.
- Comandos: `w/s/a/d` movem (custam 1 ponto cada, mesmo batendo na borda), `c` embarca o passageiro da célula, `q` aborta.
- Após cada ação válida, os inimigos `X` andam uma casa aleatória; bater em `X` ou asteroide `#` tira 1 vida.
- Pontuação: Professor +10, Engenheiro +15, Astronauta +20.
- Derrota: vidas = 0 ou pontuação ≤ 0. Vitória: todos a bordo **e** nave de volta em (0,0).
- Só vitórias entram no ranking Top 5 (arquivo local).

| Dificuldade | Passageiros | Asteroides | Inimigos | Pontos iniciais |
|---|---|---|---|---|
| Fácil | 4 | 1 | 1 | 30 |
| Médio | 5 | 2 | 2 | 20 |
| Difícil | 5 | 3 | 3 | 15 |

## 2. Problemas encontrados na versão anterior (`solidexercicio10`)

| Problema | Onde | Consequência |
|---|---|---|
| Regras do jogo misturadas com `Scanner`/`System.out` | `JogoService.jogarPartida` | Impossível reaproveitar no libGDX ou testar sem simular teclado |
| Configuração da dificuldade espalhada em `if/else` e `switch` | `criarNovaMissao`, `definirPontuacaoInicial` | Novo nível exige alterar vários métodos (viola OCP) |
| Símbolo de desenho (`getSimbolo`) dentro das entidades | `Nave`, `Asteroide`... | Domínio conhece detalhe de apresentação de console |
| `Random` criado internamente | `JogoService` | Mapa não reproduzível → sem testes de sorteio |
| Repositório decidia o corte do Top 5 | `RankingService.salvar` | Regra de negócio dentro da infraestrutura |
| Coordenadas soltas (`minX, maxX, minY, maxY` em todo método) | vários | Parâmetros repetidos, fácil de errar |

## 3. Estrutura nova

```
br.edu.unifor.missaomarte
├── dominio/         regras puras (sem I/O, sem libGDX)
│   ├── Partida          ← executa turnos e devolve eventos
│   ├── Missao           ← estado do mapa
│   ├── GeradorMissao    ← cria missões (Random injetado)
│   ├── Nave, Passageiro (Professor/Engenheiro/Astronauta), Asteroide, Inimigo
│   ├── Posicao, Limites, Direcao  ← value objects
│   └── Dificuldade, EstadoPartida, Evento, ResultadoTurno
├── aplicacao/       casos de uso
│   ├── ServicoRanking   ← regra do Top 5
│   ├── RepositorioRanking (interface/porta)
│   └── RegistroRanking
├── infraestrutura/
│   └── RepositorioRankingArquivo  ← detalhe: arquivo texto
├── console/         adaptador de interface
│   ├── JogoConsole      ← teclado → comandos; eventos → mensagens
│   └── MapaConsole      ← desenho em texto
└── Main             ← composition root (monta as dependências)
```

Dependências apontam para dentro: `console → aplicacao → dominio` e `infraestrutura → aplicacao`. O domínio não importa nada das outras camadas.

## 4. Decisões e princípios

| Decisão | Princípio | Por quê |
|---|---|---|
| `Partida.mover()`/`embarcar()` devolvem `ResultadoTurno` com `Evento`s | SRP, separação domínio/apresentação | O domínio informa *o que aconteceu*; console imprime texto, libGDX tocará som/animação |
| `Dificuldade` guarda suas quantidades e pontuação | OCP, Information Expert | Novo nível = nova constante |
| `RepositorioRanking` como interface | DIP | Teste usa repositório em memória; Fase 5 troca por cliente HTTP sem mexer no serviço |
| `ServicoRanking` decide o Top 5; repositório só grava | SRP | Regra fica na aplicação, não no arquivo |
| `GeradorMissao` recebe `Random` | Creator, testabilidade | Mesma semente = mesmo mapa |
| Símbolos (`@ # X P E T`) só em `MapaConsole` | Separação de responsabilidades | Domínio sem detalhe visual |
| `Nave.embarcar`/`perderVida` com visibilidade de pacote | Encapsulamento | Só a `Partida` altera esse estado |

**Custo:** mais classes e um passo de tradução (evento → mensagem). Aceitamos porque é isso que permite reaproveitar o domínio no libGDX e no Android.

## 5. Comportamento preservado

Mesmas regras, pontuações, quantidades, mensagens e menu da Fase 0. Mudança intencional: o arquivo de ranking agora é `ranking.txt`.

## 6. Testes (Fase 2)

22 testes JUnit 5 em `fase-02-gradle/src/test`, cobrindo movimento, borda, embarque, nave cheia, colisão, combustível, vitória, sorteio reproduzível, Top 5 e persistência em arquivo.
