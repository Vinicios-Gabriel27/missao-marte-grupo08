package br.edu.unifor.missaomarte.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.sobrevivencia.Arena;
import br.edu.unifor.missaomarte.sobrevivencia.CronogramaPassageiros;
import br.edu.unifor.missaomarte.sobrevivencia.Dificuldade;
import br.edu.unifor.missaomarte.sobrevivencia.EstadoPartida;
import br.edu.unifor.missaomarte.sobrevivencia.GeradorOndas;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.TipoInimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ServicoRankingTest {
    static class Memoria implements RepositorioRanking {
        List<RegistroRanking> dados = new ArrayList<>();

        public List<RegistroRanking> carregar() {
            return new ArrayList<>(dados);
        }

        public void salvar(List<RegistroRanking> registros) {
            dados = new ArrayList<>(registros);
        }
    }

    /** Partida encerrada por derrota após alguns segundos (pontuação = tempo × 2). */
    private PartidaSobrevivencia partidaPerdida(float segundosAntes) {
        // Depois de "segundosAntes", tanques cercam a nave até destruí-la.
        GeradorOndas cerco = (t, d, nave, a) -> t < segundosAntes ? List.of()
                : List.of(new Inimigo(TipoInimigo.TANQUE, nave.mais(Vetor.polar(t * 7, 1.2f))));
        PartidaSobrevivencia p = new PartidaSobrevivencia(Dificuldade.DIFICIL, Arena.PADRAO, new Random(1), cerco,
                CronogramaPassageiros.nenhum());
        while (!p.estado().encerrada()) {
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
            p.atualizar(0.1f, Vetor.ZERO);
        }
        return p;
    }

    private RegistroRanking registro(int pontos) {
        return new RegistroRanking("X" + pontos, pontos, "Médio", 60, 3, 10, 1, false, "01/01/2026 10:00");
    }

    @Test
    void partidaEncerradaEntraNoRankingVazio() {
        Memoria repo = new Memoria();
        ServicoRanking s = new ServicoRanking(repo);
        PartidaSobrevivencia p = partidaPerdida(10);
        assertEquals(EstadoPartida.DERROTA, p.estado());

        assertEquals(1, s.registrar("Vini", p));
        assertEquals("Vini", s.top().get(0).piloto());
        assertEquals(p.pontuacao(), s.recorde());
    }

    @Test
    void mantemSoOsDezMelhoresEmOrdem() {
        Memoria repo = new Memoria();
        for (int i = 1; i <= 10; i++) {
            repo.dados.add(registro(i * 1000));
        }
        ServicoRanking s = new ServicoRanking(repo);
        assertEquals(-1, s.registrar("Fraco", partidaPerdida(1)));
        assertEquals(10, s.top().size());
        assertEquals(10000, s.top().get(0).pontuacao());
        assertTrue(s.top().stream().noneMatch(r -> r.piloto().equals("Fraco")));
    }

    @Test
    void nomeVazioOuLongoEhNormalizado() {
        assertEquals("Piloto", ServicoRanking.normalizar("  "));
        assertEquals(16, ServicoRanking.normalizar("Um nome muito muito comprido").length());
    }

    @Test
    void partidaEmAndamentoNaoEntra() {
        PartidaSobrevivencia p = new PartidaSobrevivencia(Dificuldade.MEDIO, new Random(2));
        p.atualizar(0.1f, Vetor.ZERO);
        assertEquals(-1, new ServicoRanking(new Memoria()).registrar("A", p));
    }
}
