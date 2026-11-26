package br.edu.unifor.missaomarte.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.dominio.Dificuldade;
import br.edu.unifor.missaomarte.dominio.Direcao;
import br.edu.unifor.missaomarte.dominio.Engenheiro;
import br.edu.unifor.missaomarte.dominio.Limites;
import br.edu.unifor.missaomarte.dominio.Missao;
import br.edu.unifor.missaomarte.dominio.Nave;
import br.edu.unifor.missaomarte.dominio.Partida;
import br.edu.unifor.missaomarte.dominio.Posicao;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ServicoRankingTest {

    /** Dublê em memória: o serviço é testado sem tocar em disco (benefício do DIP). */
    static class RepositorioMemoria implements RepositorioRanking {
        List<RegistroRanking> dados = new ArrayList<>();

        public List<RegistroRanking> listar() {
            return new ArrayList<>(dados);
        }

        public void salvarTodos(List<RegistroRanking> registros) {
            dados = new ArrayList<>(registros);
        }

        public void limpar() {
            dados.clear();
        }
    }

    private Partida partidaVencida() {
        Missao m = new Missao(new Nave("T", Posicao.PLATAFORMA, 5), Limites.centrado(3));
        m.adicionar(new Engenheiro("E", new Posicao(0, 1)));
        Partida p = new Partida(m, Dificuldade.MEDIO, new Random(1));
        p.mover(Direcao.CIMA);
        p.embarcar();
        p.mover(Direcao.BAIXO);
        return p;
    }

    private RegistroRanking registro(int pontos) {
        return new RegistroRanking("X" + pontos, pontos, Dificuldade.MEDIO, 1, 10, "01/01/2026 10:00");
    }

    @Test
    void registraVitoriaNoRankingVazio() {
        RepositorioMemoria repo = new RepositorioMemoria();
        ServicoRanking servico = new ServicoRanking(repo);

        assertTrue(servico.registrar("Vini", partidaVencida(), 12));
        assertEquals("Vini", servico.top().get(0).piloto());
    }

    @Test
    void naoRegistraPartidaQueNaoFoiVencida() {
        ServicoRanking servico = new ServicoRanking(new RepositorioMemoria());
        Missao m = new Missao(new Nave("T", Posicao.PLATAFORMA, 5), Limites.centrado(3));
        m.adicionar(new Engenheiro("E", new Posicao(3, 3)));
        Partida abortada = new Partida(m, Dificuldade.MEDIO, new Random(1));
        abortada.abortar();

        assertFalse(servico.registrar("X", abortada, 1));
        assertTrue(servico.top().isEmpty());
    }

    @Test
    void mantemSoOsCincoMelhoresEmOrdem() {
        RepositorioMemoria repo = new RepositorioMemoria();
        repo.dados = new ArrayList<>(List.of(registro(5), registro(50), registro(10), registro(40), registro(20)));
        ServicoRanking servico = new ServicoRanking(repo);

        assertTrue(servico.registrar("Novo", partidaVencida(), 3)); // 33 pontos

        List<Integer> pontos = servico.top().stream().map(RegistroRanking::pontuacao).toList();
        assertEquals(List.of(50, 40, 33, 20, 10), pontos);
        assertEquals(5, repo.dados.size());
    }

    @Test
    void pontuacaoAbaixoDoQuintoNaoEntra() {
        RepositorioMemoria repo = new RepositorioMemoria();
        repo.dados = new ArrayList<>(List.of(registro(90), registro(80), registro(70), registro(60), registro(50)));
        ServicoRanking servico = new ServicoRanking(repo);

        assertFalse(servico.entraNoTop(50));
        assertTrue(servico.entraNoTop(51));
        assertFalse(servico.entraNoTop(0));
    }

    @Test
    void nomeVazioViraPilotoAnonimo() {
        assertEquals("Piloto Anônimo", ServicoRanking.normalizarNome("   "));
    }
}
