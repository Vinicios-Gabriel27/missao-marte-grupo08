package br.edu.unifor.missaomarte.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Regras de uma partida, sem nenhuma entrada/saída. Antes, essas regras estavam
 * misturadas com Scanner e System.out dentro do JogoService. Agora qualquer
 * interface chama mover()/embarcar() e lê os eventos devolvidos.
 *
 * Regras preservadas do jogo original:
 * - cada movimento custa 1 ponto (mesmo se bater no limite do mapa);
 * - embarcar soma a pontuação do passageiro e não custa ponto;
 * - depois de toda ação válida os inimigos se movem e a colisão é verificada;
 * - pontuação &lt;= 0 encerra a partida;
 * - vitória: todos a bordo e nave na plataforma (0,0).
 */
public class Partida {
    private final Missao missao;
    private final Dificuldade dificuldade;
    private final Random random;
    private int pontuacao;
    private int movimentos;
    private EstadoPartida estado = EstadoPartida.EM_ANDAMENTO;

    public Partida(Missao missao, Dificuldade dificuldade, Random random) {
        this.missao = missao;
        this.dificuldade = dificuldade;
        this.random = random;
        this.pontuacao = dificuldade.pontuacaoInicial();
    }

    public ResultadoTurno mover(Direcao direcao) {
        exigirEmAndamento();
        List<Evento> eventos = new ArrayList<>();
        boolean andou = missao.getNave().mover(direcao, missao.getLimites());
        eventos.add(andou ? Evento.NAVE_MOVEU : Evento.NAVE_BLOQUEADA_NO_LIMITE);
        pontuacao--;
        movimentos++;
        fecharTurno(eventos);
        return new ResultadoTurno(List.copyOf(eventos), Optional.empty());
    }

    public ResultadoTurno embarcar() {
        exigirEmAndamento();
        List<Evento> eventos = new ArrayList<>();
        Optional<Passageiro> alvo = missao.passageiroSobANave();
        Optional<Passageiro> embarcado = Optional.empty();
        if (alvo.isEmpty()) {
            eventos.add(Evento.NENHUM_PASSAGEIRO_AQUI);
        } else if (missao.getNave().estaCheia()) {
            eventos.add(Evento.NAVE_CHEIA);
        } else {
            missao.embarcar(alvo.get());
            pontuacao += alvo.get().getPontuacao();
            eventos.add(Evento.PASSAGEIRO_EMBARCADO);
            embarcado = alvo;
        }
        fecharTurno(eventos);
        return new ResultadoTurno(List.copyOf(eventos), embarcado);
    }

    public ResultadoTurno abortar() {
        exigirEmAndamento();
        estado = EstadoPartida.ABORTADA;
        return new ResultadoTurno(List.of(Evento.MISSAO_ABORTADA), Optional.empty());
    }

    private void fecharTurno(List<Evento> eventos) {
        missao.moverInimigos(random);
        if (missao.naveColidiu()) {
            missao.getNave().perderVida();
            eventos.add(Evento.COLISAO);
            if (missao.getNave().destruida()) {
                eventos.add(Evento.NAVE_DESTRUIDA);
                estado = EstadoPartida.DESTRUIDA;
            }
        }
        if (pontuacao <= 0) {
            eventos.add(Evento.COMBUSTIVEL_ESGOTADO);
            if (estado == EstadoPartida.EM_ANDAMENTO) {
                estado = EstadoPartida.SEM_COMBUSTIVEL;
            }
        }
        if (estado == EstadoPartida.EM_ANDAMENTO && missao.todosEmbarcados()) {
            if (missao.naveNaPlataforma()) {
                eventos.add(Evento.MISSAO_CUMPRIDA);
                estado = EstadoPartida.VITORIA;
            } else {
                eventos.add(Evento.RETORNE_A_PLATAFORMA);
            }
        }
    }

    private void exigirEmAndamento() {
        if (estado.encerrada()) {
            throw new IllegalStateException("A partida já terminou: " + estado);
        }
    }

    public Missao getMissao() {
        return missao;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getMovimentos() {
        return movimentos;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public int getPassageirosResgatados() {
        return missao.getNave().getPassageirosABordo().size();
    }
}
