# Roteiro da apresentação (10–15 min)

## Preparação (antes de entrar)
1. Terminal 1: `cd fase-07-evolucao-livre/servidor && .\gradlew.bat bootRun` (esperar "Started").
2. Terminal 2: `cd fase-07-evolucao-livre/cliente && .\gradlew.bat :lwjgl3:run`.
3. Navegador: `http://localhost:8080/swagger-ui.html` e a aba **Actions** do GitHub.
4. Plano B: se algo falhar ao vivo, usar as capturas do branch `capturas` e o APK de Releases.

## Sequência
| Tempo | O que mostrar | Fala-chave |
|---|---|---|
| 0:00–1:30 | README (tabela de fases) e GitHub Project | "Evoluímos em 7 fases sem reescrever; cada fase é uma pasta preservada." |
| 1:30–3:00 | Fase 0 rodando no console e Fase 1 (`Partida` + `Evento`) | "Tiramos `Scanner`/`println` das regras: a Partida devolve eventos." |
| 3:00–6:00 | Jogo 3D: menu, partida, passageiros (bônus), upgrades com raridade, Investida | "A tela só traduz entrada e desenha; as regras estão no pacote `sobrevivencia`." |
| 6:00–8:00 | Conta online: criar conta, jogar, ranking global; Swagger com `/api/ranking` | "Cliente e servidor independentes; JWT validado no Spring Security; DTOs." |
| 8:00–9:30 | Desligar o servidor e mostrar que o jogo continua (convidado/local) | "Requisições assíncronas com `Gdx.net`; falha não trava o jogo." |
| 9:30–11:00 | APK em Releases / código do `AndroidLauncher` e `ControleToque` | "O core é o mesmo no desktop e no Android; só muda o ponto de entrada." |
| 11:00–12:30 | Testes: `gradlew test` + aba Actions (Java 17 e 26, ponta a ponta, capturas) | "Mais de 80 testes; o domínio é testado sem abrir janela." |
| 12:30–14:00 | Fase 7: cartas (níveis, raridade), carta cruel, chefe mirando → investida → anel | "Evoluções = arma nível 5 + passiva certa (enum `Evolucao`); Strategy/State e eventos, com testes." |

## Respostas às perguntas do professor

**Como as responsabilidades mudaram entre console e gráfico?**
No console original, `Main`/`JogoService` liam teclado, aplicavam regras e imprimiam. Agora as regras estão em
`Partida`/`PartidaSobrevivencia` (domínio), que recebem comandos e devolvem eventos; console, libGDX e Android são
adaptadores que traduzem entrada e reagem aos eventos.

**Onde ficam as regras de negócio? Como os testes provam que não dependem da interface?**
No pacote `sobrevivencia` (e `dominio` nas fases 1–2), sem nenhum import da libGDX. Os testes criam a partida com
`GeradorOndas.nenhum()`, chamam `atualizar(delta, direção)` e verificam o estado — rodam no CI sem janela nem OpenGL.

**Diferença entre o cliente libGDX e o servidor Spring Boot?**
O cliente executa o jogo (tempo real, renderização, entrada) e guarda o ranking local. O servidor é independente:
guarda contas e partidas e calcula o ranking global por uma API REST; não executa o jogo.

**Como o cliente se comunica com a API? E se o servidor cair?**
`ClienteHttp` (implementa `ServicoOnline`) usa `Gdx.net.sendHttpRequest` em outra thread e entrega a resposta na thread
do jogo com `postRunnable`. Se cair, a resposta é uma falha tratada: aviso na tela e partida salva só localmente.

**Por que senha como hash? Onde o token é validado?**
Se o banco vazar, o hash BCrypt (com sal e custo) não revela a senha. O token JWT é validado pelo
`oauth2ResourceServer` do Spring Security (`ConfiguracaoSeguranca`) antes de chegar ao controller; rotas sem token
válido recebem 401.

**O que é DTO e por que usar?**
Objeto só de transferência (`Dtos.java`, records). A API não expõe as entidades JPA: o hash da senha não vaza, o
formato da API não muda se o banco mudar e a validação fica no contrato de entrada.

**Como o jogo reutiliza código entre desktop e Android?**
Todo o jogo está em `core`; `lwjgl3` e `android` só têm o ponto de entrada e configuração. Os assets são os mesmos
(`assets.srcDirs = ['../assets']`). O controle aceita teclado e joystick de toque ao mesmo tempo.

**Qual decisão de arquitetura facilitou a melhoria?**
Domínio por eventos + Strategy: a Investida implementa `PoderEspecial`, o chefe usa `ComportamentoChefe` (State) e a
tela só reage a `PODER_ATIVADO`/`CHEFE_MIRANDO`. A raridade entrou como enum com dados, sem mexer no laço.

**O que fariam diferente com mais tempo?**
Validação de pontuação no servidor por replay, opção de qualidade gráfica no Android, mais naves/poderes e biomas,
publicação em nuvem com HTTPS e banco gerenciado, e o histórico de commits/Kanban distribuído ao longo das sprints.
