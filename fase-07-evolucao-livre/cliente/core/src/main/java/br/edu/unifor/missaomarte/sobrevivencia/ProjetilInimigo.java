package br.edu.unifor.missaomarte.sobrevivencia;

/** Projétil disparado pelo chefe; machuca a nave (a investida atravessa sem dano). */
public class ProjetilInimigo {
    public static final float RAIO = 0.4f;
    static final float VELOCIDADE = 7.5f;
    static final float DANO = 12f;
    static final float DURACAO = 4f;

    private Vetor posicao;
    private final Vetor velocidade;
    private float tempo = DURACAO;

    public ProjetilInimigo(Vetor posicao, Vetor direcao) {
        this.posicao = posicao;
        this.velocidade = direcao.normalizado().vezes(VELOCIDADE);
    }

    void avancar(float delta) {
        posicao = posicao.mais(velocidade.vezes(delta));
        tempo -= delta;
    }

    boolean expirado() {
        return tempo <= 0;
    }

    public Vetor posicao() {
        return posicao;
    }
}
