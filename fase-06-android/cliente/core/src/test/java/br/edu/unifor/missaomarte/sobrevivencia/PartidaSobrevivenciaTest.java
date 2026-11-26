package br.edu.unifor.missaomarte.sobrevivencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Upgrade;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Regras do modo sobrevivência testadas sem janela: sem ondas automáticas, tudo montado à mão. */
class PartidaSobrevivenciaTest {
    private static final float QUADRO = 1f / 60f;

    private PartidaSobrevivencia partidaControlada(CronogramaPassageiros cronograma) {
        return new PartidaSobrevivencia(Dificuldade.MEDIO, Arena.PADRAO, new Random(1), GeradorOndas.nenhum(), cronograma);
    }

    private PartidaSobrevivencia partidaControlada() {
        return partidaControlada(CronogramaPassageiros.nenhum());
    }

    private void simular(PartidaSobrevivencia p, float segundos, Vetor entrada) {
        for (float t = 0; t < segundos && p.estado() == EstadoPartida.EM_ANDAMENTO; t += QUADRO) {
            p.atualizar(QUADRO, entrada);
        }
    }

    @Test
    void naveAndaNaDirecaoDoControleComAVelocidadeDosAtributos() {
        PartidaSobrevivencia p = partidaControlada();
        simular(p, 1f, new Vetor(1, 0));
        assertEquals(p.nave().atributos().velocidade(), p.nave().posicao().x(), 0.2f);
        assertEquals(0f, p.nave().posicao().y(), 1e-4f);
    }

    @Test
    void naveNaoSaiDaArena() {
        PartidaSobrevivencia p = partidaControlada();
        simular(p, 20f, new Vetor(0, 1));
        assertEquals(Arena.PADRAO.meiaLargura(), p.nave().posicao().y(), 1e-4f);
    }

    @Test
    void inimigoPersegueEEncostarTiraVida() {
        PartidaSobrevivencia p = partidaControlada();
        p.adicionarInimigo(new Inimigo(TipoInimigo.TANQUE, new Vetor(3, 0)));
        float vidaInicial = p.nave().vida();
        simular(p, 2f, Vetor.ZERO);
        assertTrue(p.nave().vida() < vidaInicial);
    }

    @Test
    void tiroAutomaticoDerrubaInimigoQueDeixaXp() {
        PartidaSobrevivencia p = partidaControlada();
        p.adicionarInimigo(new Inimigo(TipoInimigo.RAPIDO, new Vetor(8, 0)));
        List<Evento> todos = new java.util.ArrayList<>();
        for (int i = 0; i < 120; i++) {
            todos.addAll(p.atualizar(QUADRO, Vetor.ZERO));
        }
        assertTrue(todos.contains(Evento.TIRO));
        assertTrue(todos.contains(Evento.INIMIGO_DERROTADO));
        assertEquals(1, p.inimigosDerrotados());
    }

    @Test
    void ganharXpSuficienteSobeNivelEPausaParaEscolha() {
        PartidaSobrevivencia p = partidaControlada();
        p.adicionarCristal(new CristalXp(new Vetor(0.2f, 0), p.xpParaProximoNivel()));
        p.atualizar(QUADRO, Vetor.ZERO);

        assertEquals(2, p.nivel());
        assertEquals(EstadoPartida.ESCOLHENDO_UPGRADE, p.estado());
        assertEquals(3, p.opcoesUpgrade().size());

        float tempoAntes = p.tempo();
        p.atualizar(QUADRO, new Vetor(1, 0));
        assertEquals(tempoAntes, p.tempo(), 1e-6f, "jogo deve ficar pausado durante a escolha");

        p.escolherUpgrade(0);
        assertEquals(EstadoPartida.EM_ANDAMENTO, p.estado());
    }

    @Test
    void escolherUpgradeSemSubirNivelEhErro() {
        assertThrows(IllegalStateException.class, () -> partidaControlada().escolherUpgrade(0));
    }

    @Test
    void imaAtraiCristalDistante() {
        PartidaSobrevivencia p = partidaControlada();
        CristalXp c = new CristalXp(new Vetor(2.5f, 0), 1);
        p.adicionarCristal(c);
        simular(p, 0.5f, Vetor.ZERO);
        assertTrue(p.cristais().isEmpty());
        assertTrue(p.xp() > 0);
    }

    @Test
    void passageirosAplicamEAcumulamBonus() {
        PartidaSobrevivencia p = partidaControlada();
        AtributosNave a = p.nave().atributos();
        p.adicionarPassageiro(new Professor("P1", new Vetor(0.5f, 0)));
        p.adicionarPassageiro(new Professor("P2", new Vetor(0, 0.5f)));
        p.adicionarPassageiro(new Engenheiro("E", new Vetor(-0.5f, 0)));
        p.adicionarPassageiro(new Astronauta("A", new Vetor(0, -0.5f)));

        p.atualizar(QUADRO, Vetor.ZERO);

        assertEquals(4, p.passageirosResgatados());
        assertEquals(1f + 2 * Professor.BONUS_XP, a.multiplicadorXp(), 1e-5f);
        assertEquals(Engenheiro.REGENERACAO, a.regeneracaoPorSegundo(), 1e-5f);
        assertEquals(1f + Astronauta.BONUS_DANO, a.multiplicadorDano(), 1e-5f);
    }

    @Test
    void naveLevaNoMaximoCincoPassageiros() {
        PartidaSobrevivencia p = partidaControlada();
        for (int i = 0; i < 7; i++) {
            p.adicionarPassageiro(new Astronauta("A" + i, new Vetor(0.1f * i, 0)));
        }
        p.atualizar(QUADRO, Vetor.ZERO);
        assertEquals(Nave.CAPACIDADE, p.passageirosResgatados());
        assertEquals(2, p.passageirosNoMapa().size());
    }

    @Test
    void engenheiroRegeneraVidaComOTempo() {
        PartidaSobrevivencia p = partidaControlada();
        p.adicionarInimigo(new Inimigo(TipoInimigo.TANQUE, new Vetor(0.5f, 0)));
        p.atualizar(QUADRO, Vetor.ZERO);
        float ferida = p.nave().vida();
        p.inimigosParaTeste().clear();
        p.adicionarPassageiro(new Engenheiro("E", new Vetor(0.3f, 0)));
        simular(p, 3f, Vetor.ZERO);
        assertTrue(p.nave().vida() > ferida + 2.5f);
    }

    @Test
    void plataformaSoAbreComTempoMinimoETodosResgatados() {
        CronogramaPassageiros um = new CronogramaPassageiros() {
            boolean gerado;

            public List<Passageiro> gerar(float t, Vetor nave, Arena a) {
                if (gerado) {
                    return List.of();
                }
                gerado = true;
                return List.of(new Astronauta("A", new Vetor(5, 0)));
            }

            public int total() {
                return 1;
            }
        };
        PartidaSobrevivencia p = partidaControlada(um);
        simular(p, 1f, new Vetor(1, 0));
        assertEquals(1, p.passageirosResgatados());
        assertFalse(p.plataformaAberta(), "ainda não passou o tempo mínimo");

        List<Evento> eventos = new java.util.ArrayList<>();
        while (p.tempo() < PartidaSobrevivencia.TEMPO_MINIMO + 0.1f && !p.estado().encerrada()) {
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
            eventos.addAll(p.atualizar(0.1f, Vetor.ZERO));
        }
        assertTrue(p.plataformaAberta());
        assertTrue(eventos.contains(Evento.CHEFE_APARECEU));
        assertTrue(p.inimigos().stream().anyMatch(i -> i.tipo() == TipoInimigo.CHEFE));
    }

    @Test
    void pousarNaPlataformaAbertaVence() {
        CronogramaPassageiros nenhumNecessario = CronogramaPassageiros.nenhum();
        PartidaSobrevivencia p = partidaControlada(nenhumNecessario);
        simular(p, 2f, new Vetor(1, 0)); // sai da plataforma
        while (p.tempo() < PartidaSobrevivencia.TEMPO_MINIMO) {
            p.atualizar(0.5f, Vetor.ZERO);
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
        }
        p.atualizar(QUADRO, Vetor.ZERO);
        assertTrue(p.plataformaAberta());
        simular(p, 5f, new Vetor(-1, 0));
        assertEquals(EstadoPartida.VITORIA, p.estado());
        assertTrue(p.pontuacao() >= 500);
    }

    @Test
    void vidaZeradaEhDerrota() {
        PartidaSobrevivencia p = partidaControlada();
        for (int i = 0; i < 8; i++) {
            p.adicionarInimigo(new Inimigo(TipoInimigo.TANQUE, Vetor.polar(i, 1.2f)));
        }
        simular(p, 10f, Vetor.ZERO);
        assertEquals(EstadoPartida.DERROTA, p.estado());
        assertThrows(IllegalStateException.class, () -> p.escolherUpgrade(0));
    }

    @Test
    void orbitalCausaDanoEmInimigoProximo() {
        PartidaSobrevivencia p = partidaControlada();
        p.adicionarArma(new Orbital());
        Inimigo alvo = new Inimigo(TipoInimigo.TANQUE, new Vetor(Orbital.RAIO_ORBITA, 0));
        p.adicionarInimigo(alvo);
        for (int i = 0; i < 30; i++) {
            p.atualizar(QUADRO, Vetor.ZERO);
        }
        assertTrue(alvo.vida() < TipoInimigo.TANQUE.vida());
    }

    @Test
    void upgradeDeArmaFicaIndisponivelNoNivelMaximo() {
        PartidaSobrevivencia p = partidaControlada();
        for (int nivel = 0; nivel < 10; nivel++) {
            p.adicionarCristal(new CristalXp(Vetor.ZERO, p.xpParaProximoNivel()));
            p.atualizar(QUADRO, Vetor.ZERO);
            while (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                List<Upgrade> opcoes = p.opcoesUpgrade();
                assertEquals(opcoes.size(), opcoes.stream().distinct().count(), "opções repetidas");
                int orbital = -1;
                for (int i = 0; i < opcoes.size(); i++) {
                    if (opcoes.get(i).titulo().startsWith("Orbital")) {
                        orbital = i;
                    }
                }
                p.escolherUpgrade(Math.max(orbital, 0));
            }
        }
        p.arma(Orbital.class).ifPresent(o -> assertTrue(o.nivel() <= o.nivelMaximo()));
    }

    @Test
    void ondasPadraoGeramInimigosForaDaTelaEDentroDaArena() {
        GeradorOndas ondas = GeradorOndas.padrao(new Random(4), 1f);
        int gerados = 0;
        for (float t = 0; t < 180; t += 0.1f) {
            for (Inimigo i : ondas.gerar(t, 0.1f, Vetor.ZERO, Arena.PADRAO)) {
                gerados++;
                assertTrue(Arena.PADRAO.contem(i.posicao()));
                assertTrue(i.posicao().comprimento() >= 15f);
                if (t < TipoInimigo.RAPIDO.surgeAPartirDe()) {
                    assertEquals(TipoInimigo.PERSEGUIDOR, i.tipo());
                }
            }
        }
        assertTrue(gerados > 100);
    }

    @Test
    void cronogramaPadraoLiberaCincoPassageirosAntesDoTempoMinimo() {
        CronogramaPassageiros c = CronogramaPassageiros.padrao(new Random(2));
        List<Passageiro> todos = new java.util.ArrayList<>();
        for (float t = 0; t <= PartidaSobrevivencia.TEMPO_MINIMO; t += 1f) {
            todos.addAll(c.gerar(t, Vetor.ZERO, Arena.PADRAO));
        }
        assertEquals(5, todos.size());
        assertEquals(5, c.total());
    }
}
