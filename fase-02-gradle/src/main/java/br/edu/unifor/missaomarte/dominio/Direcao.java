package br.edu.unifor.missaomarte.dominio;

/** Direções de movimento. A tradução de tecla para direção fica na interface, não aqui. */
public enum Direcao {
    CIMA(0, 1), BAIXO(0, -1), ESQUERDA(-1, 0), DIREITA(1, 0);

    private final int dx;
    private final int dy;

    Direcao(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }
}
