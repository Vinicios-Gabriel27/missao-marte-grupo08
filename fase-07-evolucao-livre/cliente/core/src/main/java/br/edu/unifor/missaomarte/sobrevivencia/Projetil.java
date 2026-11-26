package br.edu.unifor.missaomarte.sobrevivencia;

/** Tiro em linha reta; some ao acertar ou depois de um tempo. */
public class Projetil {
    public static final float RAIO = 0.25f;
    static final float VELOCIDADE = 18f;
    static final float DURACAO = 1.4f;

    private Vetor posicao;
    private final Vetor velocidade;
    private final float dano;
    private float tempoRestante = DURACAO;
    private boolean consumido;

    public Projetil(Vetor posicao, Vetor direcao, float dano) {
        this.posicao = posicao;
        this.velocidade = direcao.normalizado().vezes(VELOCIDADE);
        this.dano = dano;
    }

    void avancar(float delta) {
        posicao = posicao.mais(velocidade.vezes(delta));
        tempoRestante -= delta;
    }

    void consumir() {
        consumido = true;
    }

    boolean ativo() {
        return !consumido && tempoRestante > 0;
    }

    public Vetor posicao() {
        return posicao;
    }

    public Vetor velocidade() {
        return velocidade;
    }

    public float dano() {
        return dano;
    }
}
