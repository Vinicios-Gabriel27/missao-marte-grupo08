package br.edu.unifor.missaomarte.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Testa as regras sem console nem janela: monta uma missão à mão (sem sorteio)
 * e verifica pontuação, colisão, embarque e vitória.
 */
class PartidaTest {
    private final Limites limites = Limites.centrado(3);

    private Missao missaoVazia(int capacidade) {
        return new Missao(new Nave("Teste", Posicao.PLATAFORMA, capacidade), limites);
    }

    private Partida partida(Missao missao) {
        return new Partida(missao, Dificuldade.MEDIO, new Random(42));
    }

    @Test
    void moverAndaUmaCasaECustaUmPonto() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(3, 3)));
        Partida partida = partida(missao);

        ResultadoTurno r = partida.mover(Direcao.CIMA);

        assertTrue(r.contem(Evento.NAVE_MOVEU));
        assertEquals(new Posicao(0, 1), missao.getNave().getPosicao());
        assertEquals(Dificuldade.MEDIO.pontuacaoInicial() - 1, partida.getPontuacao());
        assertEquals(1, partida.getMovimentos());
    }

    @Test
    void moverContraOLimiteNaoSaiDoMapaMasCustaPonto() {
        Missao missao = new Missao(new Nave("Teste", new Posicao(3, 0), 5), limites);
        missao.adicionar(new Professor("P", new Posicao(-3, -3)));
        Partida partida = partida(missao);

        ResultadoTurno r = partida.mover(Direcao.DIREITA);

        assertTrue(r.contem(Evento.NAVE_BLOQUEADA_NO_LIMITE));
        assertEquals(new Posicao(3, 0), missao.getNave().getPosicao());
        assertEquals(Dificuldade.MEDIO.pontuacaoInicial() - 1, partida.getPontuacao());
    }

    @Test
    void embarcarSomaPontuacaoDoPassageiro() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Astronauta("Maria", new Posicao(0, 1)));
        missao.adicionar(new Professor("Outro", new Posicao(3, 3)));
        Partida partida = partida(missao);
        partida.mover(Direcao.CIMA);

        ResultadoTurno r = partida.embarcar();

        assertTrue(r.contem(Evento.PASSAGEIRO_EMBARCADO));
        assertEquals("Maria", r.embarcado().orElseThrow().getNome());
        assertEquals(Dificuldade.MEDIO.pontuacaoInicial() - 1 + 20, partida.getPontuacao());
        assertEquals(1, partida.getPassageirosResgatados());
        assertEquals(1, missao.getPassageiros().size());
    }

    @Test
    void embarcarSemPassageiroNaoMudaPontuacao() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(3, 3)));
        Partida partida = partida(missao);

        ResultadoTurno r = partida.embarcar();

        assertTrue(r.contem(Evento.NENHUM_PASSAGEIRO_AQUI));
        assertEquals(Dificuldade.MEDIO.pontuacaoInicial(), partida.getPontuacao());
    }

    @Test
    void naveCheiaRecusaPassageiro() {
        Missao missao = missaoVazia(1);
        missao.adicionar(new Professor("A", new Posicao(0, 1)));
        missao.adicionar(new Professor("B", new Posicao(0, 2)));
        Partida partida = partida(missao);
        partida.mover(Direcao.CIMA);
        partida.embarcar();
        partida.mover(Direcao.CIMA);

        ResultadoTurno r = partida.embarcar();

        assertTrue(r.contem(Evento.NAVE_CHEIA));
        assertEquals(1, missao.getPassageiros().size());
    }

    @Test
    void colisaoComAsteroideTiraVidaETresColisoesDestroem() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(-3, -3)));
        missao.adicionar(new Asteroide(new Posicao(0, 1)));
        Partida partida = partida(missao);

        assertTrue(partida.mover(Direcao.CIMA).contem(Evento.COLISAO));
        assertEquals(Nave.VIDAS_INICIAIS - 1, missao.getNave().getVidas());
        partida.mover(Direcao.BAIXO);
        partida.mover(Direcao.CIMA);
        partida.mover(Direcao.BAIXO);
        ResultadoTurno ultima = partida.mover(Direcao.CIMA);

        assertTrue(ultima.contem(Evento.NAVE_DESTRUIDA));
        assertEquals(EstadoPartida.DESTRUIDA, partida.getEstado());
    }

    @Test
    void pontuacaoZeradaEncerraPartida() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(3, 3)));
        Partida partida = new Partida(missao, Dificuldade.DIFICIL, new Random(1));

        for (int i = 0; i < Dificuldade.DIFICIL.pontuacaoInicial(); i++) {
            partida.mover(i % 2 == 0 ? Direcao.ESQUERDA : Direcao.DIREITA);
        }

        assertEquals(EstadoPartida.SEM_COMBUSTIVEL, partida.getEstado());
    }

    @Test
    void todosABordoForaDaPlataformaPedeRetorno() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Engenheiro("E", new Posicao(0, 1)));
        Partida partida = partida(missao);
        partida.mover(Direcao.CIMA);

        ResultadoTurno r = partida.embarcar();

        assertTrue(r.contem(Evento.RETORNE_A_PLATAFORMA));
        assertFalse(partida.getEstado().encerrada());
    }

    @Test
    void voltarParaPlataformaComTodosVence() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Engenheiro("E", new Posicao(0, 1)));
        Partida partida = partida(missao);
        partida.mover(Direcao.CIMA);
        partida.embarcar();

        ResultadoTurno r = partida.mover(Direcao.BAIXO);

        assertTrue(r.contem(Evento.MISSAO_CUMPRIDA));
        assertEquals(EstadoPartida.VITORIA, partida.getEstado());
        assertEquals(Dificuldade.MEDIO.pontuacaoInicial() - 2 + 15, partida.getPontuacao());
    }

    @Test
    void partidaEncerradaNaoAceitaComandos() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(3, 3)));
        Partida partida = partida(missao);
        partida.abortar();

        assertEquals(EstadoPartida.ABORTADA, partida.getEstado());
        assertThrows(IllegalStateException.class, () -> partida.mover(Direcao.CIMA));
    }

    @Test
    void inimigoNuncaSaiDoMapa() {
        Missao missao = missaoVazia(5);
        missao.adicionar(new Professor("P", new Posicao(3, 3)));
        Inimigo inimigo = new Inimigo(new Posicao(-3, -3));
        missao.adicionar(inimigo);
        Random random = new Random(7);

        for (int i = 0; i < 500; i++) {
            inimigo.vagar(random, limites);
            assertTrue(limites.contem(inimigo.getPosicao()));
        }
    }
}
