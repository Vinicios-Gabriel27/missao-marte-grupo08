package br.edu.unifor.missaomarte.servidor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.unifor.missaomarte.servidor.repositorio.JogadorRepositorio;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Testes da API pela camada HTTP (MockMvc + H2 em memória). Cada teste usa nomes próprios. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    JogadorRepositorio jogadores;

    private ResultActions postar(String url, String json, String token) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return mvc.perform(req);
    }

    private String credenciais(String nome, String senha) {
        return "{\"nome\":\"" + nome + "\",\"senha\":\"" + senha + "\"}";
    }

    private String cadastrarELogar(String nome) throws Exception {
        postar("/api/jogadores", credenciais(nome, "senha123"), null).andExpect(status().isCreated());
        String corpo = postar("/api/sessoes", credenciais(nome, "senha123"), null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    private String partida(int pontos, int segundos, int abates, int passageiros, boolean venceu) {
        return "{\"pontuacao\":" + pontos + ",\"dificuldade\":\"MEDIO\",\"tempoSegundos\":" + segundos
                + ",\"nivel\":5,\"abates\":" + abates + ",\"passageiros\":" + passageiros + ",\"venceu\":" + venceu + "}";
    }

    @Test
    void cadastroDevolveJogadorSemSenha() throws Exception {
        postar("/api/jogadores", credenciais("Ana", "segredo1"), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void senhaEhGuardadaComoHash() throws Exception {
        postar("/api/jogadores", credenciais("Beto", "minhasenha"), null).andExpect(status().isCreated());
        String hash = jogadores.findByNomeIgnoreCase("Beto").orElseThrow().getSenhaHash();
        assertThat(hash).isNotEqualTo("minhasenha").startsWith("$2");
    }

    @Test
    void nomeRepetidoDaConflitoMesmoComOutraCaixa() throws Exception {
        postar("/api/jogadores", credenciais("Carla", "senha123"), null).andExpect(status().isCreated());
        postar("/api/jogadores", credenciais("CARLA", "outra123"), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NOME_EM_USO"));
    }

    @Test
    void dadosInvalidosListamOsCampos() throws Exception {
        postar("/api/jogadores", credenciais("x", "123"), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void loginComSenhaErradaFalha() throws Exception {
        postar("/api/jogadores", credenciais("Davi", "certa123"), null).andExpect(status().isCreated());
        postar("/api/sessoes", credenciais("Davi", "errada123"), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
        postar("/api/sessoes", credenciais("NaoExiste", "qualquer1"), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarPartidaExigeToken() throws Exception {
        postar("/api/partidas", partida(100, 50, 0, 0, false), null).andExpect(status().isUnauthorized());
        postar("/api/partidas", partida(100, 50, 0, 0, false), "token.invalido.aqui").andExpect(status().isUnauthorized());
    }

    @Test
    void partidaValidaEntraNoHistoricoEPerfil() throws Exception {
        String token = cadastrarELogar("Eva");
        postar("/api/partidas", partida(1200, 300, 40, 5, true), token)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partida.jogador").value("Eva"))
                .andExpect(jsonPath("$.posicaoGlobal").isNumber());
        mvc.perform(get("/api/partidas/minhas").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pontuacao").value(1200));
        mvc.perform(get("/api/jogadores/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidas").value(1))
                .andExpect(jsonPath("$.vitorias").value(1))
                .andExpect(jsonPath("$.melhorPontuacao").value(1200));
    }

    @Test
    void pontuacaoImpossivelEhRecusada() throws Exception {
        String token = cadastrarELogar("Fabio");
        // 10 abates + 60 s + 0 passageiros permitem no máximo 220 pontos
        postar("/api/partidas", partida(99999, 60, 10, 0, false), token)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("PONTUACAO_IMPLAUSIVEL"));
        // vitória exige os 5 passageiros
        postar("/api/partidas", partida(500, 300, 10, 2, true), token)
                .andExpect(status().is(422));
    }

    @Test
    void camposForaDosLimitesSaoRecusados() throws Exception {
        String token = cadastrarELogar("Gabi");
        postar("/api/partidas", "{\"pontuacao\":-5,\"dificuldade\":\"MEDIO\",\"tempoSegundos\":10,\"nivel\":1,"
                + "\"abates\":0,\"passageiros\":9,\"venceu\":false}", token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pontuacao").exists())
                .andExpect(jsonPath("$.campos.passageiros").exists());
        postar("/api/partidas", "{\"pontuacao\":1,\"dificuldade\":\"IMPOSSIVEL\"}", token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"));
    }

    @Test
    void rankingEhPublicoEOrdenado() throws Exception {
        String a = cadastrarELogar("Hugo");
        String b = cadastrarELogar("Iris");
        postar("/api/partidas", partida(300, 100, 10, 0, false), a).andExpect(status().isCreated());
        postar("/api/partidas", partida(8000, 2000, 400, 3, false), b).andExpect(status().isCreated());
        mvc.perform(get("/api/ranking").param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].jogador").value("Iris"));
        mvc.perform(get("/api/ranking").param("dificuldade", "FACIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historicoMostraSoAsPartidasDoProprioJogador() throws Exception {
        String a = cadastrarELogar("Joao");
        String b = cadastrarELogar("Kiko");
        postar("/api/partidas", partida(100, 50, 0, 0, false), a).andExpect(status().isCreated());
        mvc.perform(get("/api/partidas/minhas").header(HttpHeaders.AUTHORIZATION, "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void saudeEDocumentacaoSaoPublicas() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
