package br.edu.unifor.missaomarte.sobrevivencia;

/** XP deixado por inimigos. Dentro do alcance do ímã, voa até a nave. */
public class CristalXp {
    static final float VELOCIDADE_IMA = 14f;
    static final float RAIO_COLETA = 0.9f;

    private Vetor posicao;
    private final int valor;

    public CristalXp(Vetor posicao, int valor) {
        this.posicao = posicao;
        this.valor = valor;
    }

    void atrairPara(Vetor alvo, float delta) {
        Vetor dir = alvo.menos(posicao).normalizado();
        posicao = posicao.mais(dir.vezes(VELOCIDADE_IMA * delta));
    }

    public Vetor posicao() {
        return posicao;
    }

    public int valor() {
        return valor;
    }
}
