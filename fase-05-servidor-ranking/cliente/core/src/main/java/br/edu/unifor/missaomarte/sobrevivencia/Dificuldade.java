package br.edu.unifor.missaomarte.sobrevivencia;

/** Mesmos três níveis do jogo original, agora com os parâmetros do modo sobrevivência (OCP). */
public enum Dificuldade {
    FACIL("Fácil", 150, 0.7f),
    MEDIO("Médio", 100, 1.0f),
    DIFICIL("Difícil", 75, 1.4f);

    private final String rotulo;
    private final int vidaInicial;
    private final float multiplicadorInimigos;

    Dificuldade(String rotulo, int vidaInicial, float multiplicadorInimigos) {
        this.rotulo = rotulo;
        this.vidaInicial = vidaInicial;
        this.multiplicadorInimigos = multiplicadorInimigos;
    }

    public int vidaInicial() {
        return vidaInicial;
    }

    public float multiplicadorInimigos() {
        return multiplicadorInimigos;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
