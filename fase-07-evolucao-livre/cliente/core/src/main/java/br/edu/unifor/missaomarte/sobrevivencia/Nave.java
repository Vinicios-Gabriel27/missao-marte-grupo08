package br.edu.unifor.missaomarte.sobrevivencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Nave do jogador no modo sobrevivência. */
public class Nave {
    public static final int CAPACIDADE = 5;
    public static final float RAIO = 0.7f;

    private Vetor posicao = Vetor.ZERO;
    private Vetor direcao = new Vetor(0, 1);
    private final AtributosNave atributos;
    private float vida;
    private float invulneravel;
    private final List<Passageiro> aBordo = new ArrayList<>();

    public Nave(float vidaInicial) {
        this.atributos = new AtributosNave(vidaInicial);
        this.vida = vidaInicial;
    }

    void mover(Vetor entrada, float delta, Arena arena) {
        Vetor dir = entrada.normalizado();
        if (dir != Vetor.ZERO) {
            direcao = dir;
            posicao = arena.limitar(posicao.mais(dir.vezes(atributos.velocidade() * delta)));
        }
    }

    void sofrerDano(float dano) {
        if (invulneravel > 0) {
            return;
        }
        vida = Math.max(0, vida - dano * (1f - atributos.armadura()));
    }

    void ficarInvulneravel(float segundos) {
        invulneravel = Math.max(invulneravel, segundos);
    }

    void passarTempo(float delta) {
        invulneravel = Math.max(0, invulneravel - delta);
    }

    public boolean invulneravel() {
        return invulneravel > 0;
    }

    /** Movimento forçado (investida), sem mudar a direção da nave. */
    void deslocar(Vetor deslocamento, Arena arena) {
        posicao = arena.limitar(posicao.mais(deslocamento));
    }

    void curar(float valor) {
        vida = Math.min(atributos.vidaMaxima(), vida + valor);
    }

    boolean temEspaco() {
        return aBordo.size() < CAPACIDADE;
    }

    void embarcar(Passageiro p) {
        aBordo.add(p);
        p.aplicarBonus(atributos);
    }

    public boolean destruida() {
        return vida <= 0;
    }

    public Vetor posicao() {
        return posicao;
    }

    public Vetor direcao() {
        return direcao;
    }

    public float vida() {
        return vida;
    }

    public AtributosNave atributos() {
        return atributos;
    }

    public List<Passageiro> passageirosABordo() {
        return Collections.unmodifiableList(aBordo);
    }
}
