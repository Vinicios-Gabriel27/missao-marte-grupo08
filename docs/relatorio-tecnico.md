# Relatório Técnico — Projeto Integrador: Evolução do Missão Marte Unifor

**Disciplina:** T200 — Projeto de Arquitetura de Sistemas · **Equipe:** Grupo08-2026 · **Entrega:** 03/12/2026
**Repositório:** https://github.com/Vinicios-Gabriel27/missao-marte-grupo08

## 1. Visão geral

O jogo de console *Missão Marte* (resgatar passageiros e voltar à plataforma) foi evoluído em sete fases
independentes até um jogo 3D de sobrevivência (estilo *survivors*, inspirado em Megabonk e The Deep Horde),
multiplataforma (desktop e Android), com servidor de ranking global. A mudança de gênero foi autorizada pelo
professor; o núcleo do original foi mantido (passageiros, plataforma, vidas/pontuação, dificuldades, ranking).

| Fase | Entrega |
|---|---|
| 0 | Projeto inicial preservado |
| 1 | Regras separadas da interface (`Partida` devolve eventos; console só traduz) |
| 2 | Gradle Wrapper + 22 testes JUnit 5 |
| 3 | Domínio de sobrevivência em Java puro + cliente libGDX 3D (gdx-gltf, PBR, modelos Kenney CC0) |
| 4 | Menu 3D, fontes TTF, sons, HUD, ranking local, pós-processamento (bloom) |
| 5 | Servidor Spring Boot (JWT, BCrypt, JPA/H2, DTOs) + cliente online assíncrono |
| 6 | Módulo Android, joystick virtual, APK gerado no CI |
| 7 | Raridade de upgrades, poder especial (Investida), chefe com padrões de ataque |

## 2. Arquitetura e decisões técnicas

**Camadas (cliente):** `sobrevivencia` (domínio, sem libGDX) → `ranking`/`online` (aplicação e portas) →
`gdx` (apresentação e adaptadores) → `lwjgl3`/`android` (pontos de entrada). As dependências apontam para dentro.

**Laço do jogo sem dependência da tela.** A tela chama `partida.atualizar(delta, direção)` e desenha o estado:

```java
// TelaJogo.render — só tradução de entrada e reação a eventos
Vetor entrada = teclado.comprimento() > 0 ? teclado : toque.direcao();
reagir(partida.atualizar(delta, entrada));      // List<Evento>: TIRO, NAVE_ATINGIDA, CHEFE_MIRANDO...
mundo.desenhar(alvoCamera, cenario.instanciasPbr, this::desenharPbr, this::desenharEfeitos);
```

**Eventos do domínio.** O domínio informa *fatos* (`Evento`); console, libGDX e Android decidem som, partícula
ou texto. Na Fase 1 isso eliminou `System.out` das regras; nas fases seguintes permitiu adicionar som, tremor de
câmera e números de dano sem tocar no domínio.

**Servidor independente.** Projeto Gradle separado, sem código do cliente (tem seu próprio enum de dificuldade).
Contrato por DTOs (`record`), erros padronizados (RFC 9457) e segurança stateless (JWT validado pelo
Spring Security antes do controller). Senhas com BCrypt; segredo do token só por variável de ambiente.

**Integração sem bloquear a interface.** `ClienteHttp` usa `Gdx.net.sendHttpRequest` (outra thread) e devolve o
resultado com `Gdx.app.postRunnable`. Servidor fora do ar → jogo segue, partida fica no ranking local.

**Renderização.** `Mundo3D` concentra a ordem das passadas (sombras → PBR → efeitos → bloom/cor/vinheta) e é
criado uma vez; cada tela só diz *o que* desenhar. Recursos caros (modelos, shaders, fontes, sons) são carregados
em `create()` e liberados em `dispose()`, respeitando o ciclo de vida da libGDX.

## 3. Padrões e princípios aplicados

| Princípio/padrão | Onde | Por que foi útil |
|---|---|---|
| SRP | `Partida` (regras), `JogoConsole`/`TelaJogo` (entrada/saída), `ServicoRanking` (regra do top) vs `RepositorioRanking*` (persistência) | Cada classe muda por um motivo; testes focados |
| OCP | `Dificuldade`, `TipoInimigo`, `Raridade`, `UpgradeAtributo` (enums com dados); `Passageiro` polimórfico | Novo nível, inimigo ou passageiro sem alterar o laço do jogo |
| LSP | `Professor`/`Engenheiro`/`Astronauta` substituem `Passageiro` em `aplicarBonus` | A partida trata todos igual |
| ISP | `CampoDeBatalha`: as armas veem só 9 operações, não a `Partida` inteira | Arma nova não depende de detalhes do laço |
| DIP | `RepositorioRanking` (arquivo local ↔ servidor), `ServicoOnline` (HTTP ↔ dublê), `GeradorOndas`/`CronogramaPassageiros` injetados | Trocar implementação e testar sem rede/disco/sorteio |
| Strategy | `Arma` (tiro, orbital, onda), `PoderEspecial` (Investida) | Comportamentos intercambiáveis |
| State | `ComportamentoChefe` (perseguir → mirar → investir → anel) | Padrão do chefe isolado e testável |
| GRASP Creator / Information Expert | `GeradorMissao`/`GeradorOndas` criam entidades; `Missao`/`Arena` respondem sobre o mapa | Responsabilidade onde está a informação |
| Controller (GRASP) | `TelaJogo` coordena entrada → domínio → desenho | Interface sem regra de negócio |

## 4. Testes e resultados

| Conjunto | Qtde | Cobre |
|---|---|---|
| Fase 2 (console) | 22 | movimento, borda, embarque, nave cheia, colisão, combustível, vitória, sorteio reproduzível, top 5, arquivo |
| Domínio sobrevivência | 17 | movimento/arena, perseguição, tiro, XP e nível (pausa), ímã, bônus e capacidade de passageiros, plataforma, vitória, derrota, orbital, limites de arma, ondas, cronograma |
| Ranking local | 4 | top 10, ordenação, normalização, partida em andamento |
| Conta online | 5 | convidado, cadastro+login, falha, servidor fora do ar, token expirado |
| Fase 7 | 9 | pesos e efeito da raridade, arma épica, Investida (deslocamento, recarga, dano, invulnerabilidade), ciclo do chefe, anéis |
| Servidor (MockMvc + H2) | 13 | cadastro, hash, conflito, validação, login, 401, histórico, perfil, plausibilidade, limites, ranking, isolamento, saúde/docs |

**Integração contínua (GitHub Actions):** a cada envio compila e testa todas as fases com **Java 17 e 26**, sobe o
servidor e roda `teste-api.sh` (ponta a ponta), **executa o jogo em monitor virtual** (Xvfb) com piloto automático
conectado ao servidor e salva capturas de tela, e **gera o APK Android** (publicado em Releases).
**Balanceamento:** simulações automáticas (bot que desvia) — vitórias: Fácil 5/6, Médio 6/6, Difícil 4/6.

## 5. Dificuldades, limitações e melhorias futuras

- **Java 26 vs Gradle 8.7:** o wrapper da base não roda no JDK 26 → migração para Gradle 9.8.2 e, no Android, AGP 9.4.1.
- **Texturas externas em GLB** (Mini Characters) não suportadas pelo carregador → textura embutida nos arquivos.
- **Til da fonte Orbitron** parecia crase → glifo redesenhado na fonte (licença OFL permite).
- **Servidor não valida a jogabilidade:** recusa só o impossível (teto da fórmula `abates×10 + s×2 + passageiros×100 + 500`,
  vitória sem 5 passageiros, limites). Melhoria: replays verificáveis ou simulação no servidor.
- **Android:** verificado por compilação/APK no CI; desempenho real depende do aparelho (PBR + sombras + bloom).
  Melhoria: opção de qualidade gráfica.
- **Publicação em nuvem:** servidor preparado (porta, pasta de dados, segredo e endereço por variáveis de ambiente,
  `/actuator/health`), sem publicação real (não exigida). Em produção: banco gerenciado, Flyway e HTTPS.
- Futuro: mais naves com poderes diferentes, biomas, missões/conquistas no servidor, controle (gamepad).

## 6. Participação dos integrantes

| Integrante | Contribuições |
|---|---|
| Vinicius Gabriel (@Vinicios-Gabriel27) | Design do jogo (gênero, passageiros com bônus, upgrades), integração, testes no desktop, apresentação |
| _(preencher)_ | |
| _(preencher)_ | |

Ferramentas: o desenvolvimento contou com apoio de IA (Claude) para geração e revisão de código, registrado como
coautor nos commits. Recursos de terceiros: modelos Kenney (CC0), fontes Orbitron e Rajdhani (OFL); sons e música
sintetizados para o projeto.
