package br.edu.unifor.missaomarte.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A nave do jogador: posição, vidas e passageiros a bordo. */
public class Nave extends EntidadeMapa {
    public static final int VIDAS_INICIAIS = 3;

    private final String nome;
    private final int capacidade;
    private final List<Passageiro> aBordo = new ArrayList<>();
    private int vidas = VIDAS_INICIAIS;

    public Nave(String nome, Posicao posicao, int capacidade) {
        super(posicao);
        this.nome = nome;
        this.capacidade = capacidade;
    }

    /** Move uma casa se o destino estiver dentro dos limites. Retorna true se andou. */
    public boolean mover(Direcao direcao, Limites limites) {
        Posicao destino = posicao.deslocar(direcao.dx(), direcao.dy());
        if (!limites.contem(destino)) {
            return false;
        }
        posicao = destino;
        return true;
    }

    public boolean estaCheia() {
        return aBordo.size() >= capacidade;
    }

    void embarcar(Passageiro passageiro) {
        if (estaCheia()) {
            throw new IllegalStateException("Nave cheia");
        }
        aBordo.add(passageiro);
    }

    void perderVida() {
        vidas = Math.max(0, vidas - 1);
    }

    public boolean destruida() {
        return vidas == 0;
    }

    public String getNome() {
        return nome;
    }

    public int getVidas() {
        return vidas;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public List<Passageiro> getPassageirosABordo() {
        return Collections.unmodifiableList(aBordo);
    }
}
