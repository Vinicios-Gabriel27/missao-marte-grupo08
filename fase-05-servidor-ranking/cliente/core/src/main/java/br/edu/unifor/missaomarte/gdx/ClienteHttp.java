package br.edu.unifor.missaomarte.gdx;

import br.edu.unifor.missaomarte.online.Resposta;
import br.edu.unifor.missaomarte.online.ServicoOnline;
import br.edu.unifor.missaomarte.online.Sessao;
import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementação HTTP de {@link ServicoOnline} com a API de rede da libGDX. A requisição roda
 * em outra thread e a resposta volta para a thread do jogo com {@code postRunnable}, então a
 * tela continua desenhando enquanto espera (e se o servidor cair, o jogo segue normal).
 */
class ClienteHttp implements ServicoOnline {
    private static final int TEMPO_LIMITE_MS = 6000;

    private final Supplier<String> enderecoBase;
    private final JsonReader leitor = new JsonReader();

    ClienteHttp(Supplier<String> enderecoBase) {
        this.enderecoBase = enderecoBase;
    }

    @Override
    public void cadastrar(String nome, String senha, Resposta<Void> resposta) {
        enviar(Net.HttpMethods.POST, "/api/jogadores", credenciais(nome, senha), null, resposta, j -> null);
    }

    @Override
    public void entrar(String nome, String senha, Resposta<Sessao> resposta) {
        enviar(Net.HttpMethods.POST, "/api/sessoes", credenciais(nome, senha), null, resposta,
                j -> new Sessao(j.get("jogador").getString("nome"), j.getString("token")));
    }

    @Override
    public void enviarPartida(Sessao sessao, RegistroRanking p, Resposta<Long> resposta) {
        String corpo = "{\"pontuacao\":" + p.pontuacao() + ",\"dificuldade\":\"" + codigoDificuldade(p.dificuldade())
                + "\",\"tempoSegundos\":" + p.tempoSegundos() + ",\"nivel\":" + p.nivel() + ",\"abates\":" + p.abates()
                + ",\"passageiros\":" + p.passageiros() + ",\"venceu\":" + p.venceu() + "}";
        enviar(Net.HttpMethods.POST, "/api/partidas", corpo, sessao.token(), resposta, j -> j.getLong("posicaoGlobal"));
    }

    @Override
    public void rankingGlobal(Resposta<List<RegistroRanking>> resposta) {
        enviar(Net.HttpMethods.GET, "/api/ranking?limite=10", null, null, resposta, j -> {
            List<RegistroRanking> lista = new ArrayList<>();
            for (JsonValue l = j.child; l != null; l = l.next) {
                lista.add(new RegistroRanking(l.getString("jogador"), l.getInt("pontuacao"),
                        rotuloDificuldade(l.getString("dificuldade")), l.getInt("tempoSegundos"), l.getInt("nivel"),
                        0, 0, l.getBoolean("venceu"), l.getString("registradaEm", "")));
            }
            return lista;
        });
    }

    private <T> void enviar(String metodo, String caminho, String corpo, String token, Resposta<T> resposta,
                            Function<JsonValue, T> converter) {
        HttpRequestBuilder b = new HttpRequestBuilder().newRequest().method(metodo)
                .url(enderecoBase.get() + caminho).timeout(TEMPO_LIMITE_MS)
                .header("Accept", "application/json");
        if (corpo != null) {
            b.header("Content-Type", "application/json; charset=utf-8").content(corpo);
        }
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        Gdx.net.sendHttpRequest(b.build(), new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse r) {
                int status = r.getStatus().getStatusCode();
                String texto = r.getResultAsString();
                Gdx.app.postRunnable(() -> tratar(status, texto, resposta, converter));
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> resposta.falha("Servidor indisponível (" + enderecoBase.get() + ")"));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> resposta.falha("Requisição cancelada"));
            }
        });
    }

    private <T> void tratar(int status, String texto, Resposta<T> resposta, Function<JsonValue, T> converter) {
        try {
            if (status >= 200 && status < 300) {
                resposta.sucesso(converter.apply(texto == null || texto.isBlank() ? new JsonValue(JsonValue.ValueType.nullValue)
                        : leitor.parse(texto)));
            } else if (status == 401 && texto != null && !texto.contains("CREDENCIAIS_INVALIDAS")) {
                resposta.falha("Sessão expirada. Entre novamente.");
            } else {
                resposta.falha(mensagemDeErro(status, texto));
            }
        } catch (RuntimeException e) {
            resposta.falha("Resposta inesperada do servidor");
        }
    }

    private String mensagemDeErro(int status, String texto) {
        try {
            JsonValue j = leitor.parse(texto);
            JsonValue campos = j.get("campos");
            if (campos != null && campos.child != null) {
                return campos.child.name + ": " + campos.child.asString();
            }
            return j.getString("detail", "Erro " + status);
        } catch (RuntimeException e) {
            return "Erro " + status;
        }
    }

    private static String credenciais(String nome, String senha) {
        return "{\"nome\":\"" + escapar(nome) + "\",\"senha\":\"" + escapar(senha) + "\"}";
    }

    static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    static String codigoDificuldade(String rotulo) {
        return switch (rotulo) {
            case "Fácil" -> "FACIL";
            case "Difícil" -> "DIFICIL";
            default -> "MEDIO";
        };
    }

    static String rotuloDificuldade(String codigo) {
        return switch (codigo) {
            case "FACIL" -> "Fácil";
            case "DIFICIL" -> "Difícil";
            default -> "Médio";
        };
    }
}
