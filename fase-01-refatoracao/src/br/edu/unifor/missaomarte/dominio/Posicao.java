package br.edu.unifor.missaomarte.dominio;

/** Coordenada imutável no mapa. Value object: duas posições com o mesmo x/y são iguais. */
public record Posicao(int x, int y) {
    public static final Posicao PLATAFORMA = new Posicao(0, 0);

    public Posicao deslocar(int dx, int dy) {
        return new Posicao(x + dx, y + dy);
    }
}
