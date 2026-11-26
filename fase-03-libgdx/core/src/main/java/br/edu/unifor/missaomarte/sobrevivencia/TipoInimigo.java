package br.edu.unifor.missaomarte.sobrevivencia;

/** Dados de cada inimigo. Um tipo novo é uma nova constante; o laço do jogo não muda. */
public enum TipoInimigo {
    PERSEGUIDOR(10, 2.6f, 8, 1, 0, 0.6f),
    RAPIDO(4, 4.6f, 5, 1, 60, 0.45f),
    TANQUE(60, 1.4f, 25, 5, 120, 1.0f),
    CHEFE(800, 1.8f, 40, 50, Float.MAX_VALUE, 2.0f);

    private final float vida;
    private final float velocidade;
    private final float danoContato;
    private final int xp;
    private final float surgeAPartirDe;
    private final float raio;

    TipoInimigo(float vida, float velocidade, float danoContato, int xp, float surgeAPartirDe, float raio) {
        this.vida = vida;
        this.velocidade = velocidade;
        this.danoContato = danoContato;
        this.xp = xp;
        this.surgeAPartirDe = surgeAPartirDe;
        this.raio = raio;
    }

    public float vida() {
        return vida;
    }

    public float velocidade() {
        return velocidade;
    }

    public float danoContato() {
        return danoContato;
    }

    public int xp() {
        return xp;
    }

    /** Segundos de partida a partir dos quais o tipo entra nas ondas comuns. */
    public float surgeAPartirDe() {
        return surgeAPartirDe;
    }

    public float raio() {
        return raio;
    }
}
