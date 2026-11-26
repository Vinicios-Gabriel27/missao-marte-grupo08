# Fase 5 — Servidor e ranking global

Dois projetos **independentes** (o servidor não usa nenhum código do cliente):

| Pasta | O quê | Tecnologias |
|---|---|---|
| `servidor/` | API REST: cadastro, login, partidas, histórico, ranking global | Spring Boot 4.1, Spring Security (JWT HS256), JPA + H2, Bean Validation, springdoc (Swagger), Actuator |
| `cliente/` | Jogo da Fase 4 + conta online, envio de partidas e ranking global | libGDX, gdx-gltf, `Gdx.net` (HTTP assíncrono) |

## Executar

Dois terminais (JDK 17+):

```bash
# 1) servidor em http://localhost:8080
cd fase-05-servidor-ranking/servidor
./gradlew bootRun          # Windows: .\gradlew.bat bootRun

# 2) cliente
cd fase-05-servidor-ranking/cliente
./gradlew :lwjgl3:run      # Windows: .\gradlew.bat :lwjgl3:run
```

No menu: **CONTA → CRIAR CONTA** (ou ENTRAR). Sem conta, o jogo funciona como **convidado** (ranking local),
inclusive com o servidor desligado.

Testes: `./gradlew test` em cada pasta. Teste de ponta a ponta com o servidor rodando: `servidor/teste-api.sh`.

## API

Documentação interativa: **http://localhost:8080/swagger-ui.html**

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| POST | `/api/jogadores` | pública | Cadastra `{nome, senha}` → 201 `{id, nome, criadoEm}` |
| POST | `/api/sessoes` | pública | Login → `{token, tipo:"Bearer", expiraEm, jogador}` |
| GET | `/api/jogadores/eu` | Bearer | Perfil: partidas, vitórias, melhor pontuação, últimas partidas |
| POST | `/api/partidas` | Bearer | Registra `{pontuacao, dificuldade, tempoSegundos, nivel, abates, passageiros, venceu}` → 201 com posição global |
| GET | `/api/partidas/minhas` | Bearer | Histórico das últimas 20 partidas do jogador |
| GET | `/api/ranking?dificuldade=&limite=` | pública | Ranking global (máx. 50) |
| GET | `/actuator/health` | pública | Verificação de saúde |

Erros sempre no mesmo formato (RFC 9457): `{type, title, status, detail, codigo, campos?}`.
Códigos: `DADOS_INVALIDOS` (400), `JSON_INVALIDO` (400), `CREDENCIAIS_INVALIDAS` (401), `NOME_EM_USO` (409),
`PONTUACAO_IMPLAUSIVEL` (422). Sem token ou token inválido: 401.

## Segurança

- Senhas guardadas como **hash BCrypt**; a senha nunca é devolvida nem registrada em log.
- **Token JWT** assinado (HS256), validado pelo Spring Security antes de chegar aos controllers.
- Segredo do token **só por variável de ambiente** (`MISSAOMARTE_JWT_SEGREDO`, ≥ 32 caracteres). Sem ela, o
  servidor gera uma chave temporária a cada início.
- **DTOs** na API: entidades JPA (com o hash) nunca são expostas.
- Login com nome inexistente leva o mesmo tempo que senha errada (não revela quais nomes existem).
- O token do cliente fica só na memória (não é gravado em disco).

## Configuração (variáveis de ambiente)

| Variável | Padrão | Uso |
|---|---|---|
| `PORT` | `8080` | porta HTTP |
| `MISSAOMARTE_ENDERECO` | `127.0.0.1` | `0.0.0.0` para aceitar celulares na rede |
| `MISSAOMARTE_DADOS` | `./dados` | pasta do banco H2 (arquivo persistente) |
| `MISSAOMARTE_JWT_SEGREDO` | — | segredo do token |
| `MISSAOMARTE_JWT_VALIDADE` | `120` | validade do token (minutos) |
| `MISSAOMARTE_SERVIDOR` (cliente) | `http://localhost:8080` | endereço do servidor; também editável no menu CONTA |

## Integração no cliente (sem travar o jogo)

`ServicoOnline` (interface, pacote `online`) é implementado por `gdx/ClienteHttp` com `Gdx.net.sendHttpRequest`:
a requisição roda em outra thread e a resposta volta para a thread do jogo via `Gdx.app.postRunnable`.
`ContaOnline` concentra as regras (convidado × logado, sessão expirada) e é testada com um servidor falso.
Se o servidor estiver fora do ar, o jogo segue normal e a partida fica no ranking local.

## Limitações

- **O servidor não executa a lógica do jogo**, então não pode provar que uma pontuação é legítima. Ele recusa
  apenas o impossível: pontuação acima do teto da fórmula do jogo
  (`abates×10 + segundos×2 + passageiros×100 + 500 se venceu`), vitória sem os 5 passageiros e campos fora dos limites.
- H2 em arquivo é adequado para o projeto; em produção usaríamos um banco gerenciado e migrações (Flyway).
- Sem HTTPS local: em publicação real, o servidor deve ficar atrás de HTTPS.
