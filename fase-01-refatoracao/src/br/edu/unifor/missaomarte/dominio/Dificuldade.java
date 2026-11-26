package br.edu.unifor.missaomarte.dominio;

/**
 * Cada nível carrega sua própria configuração. Antes, os números ficavam espalhados em
 * if/else e switch dentro do JogoService; agora um nível novo é só uma nova constante (OCP).
 */
public enum Dificuldade {
    FACIL("Fácil", 4, 1, 1, 30),
    MEDIO("Médio", 5, 2, 2, 20),
    DIFICIL("Difícil", 5, 3, 3, 15);

    private final String rotulo;
    private final int passageiros;
    private final int asteroides;
    private final int inimigos;
    private final int pontuacaoInicial;

    Dificuldade(String rotulo, int passageiros, int asteroides, int inimigos, int pontuacaoInicial) {
        this.rotulo = rotulo;
        this.passageiros = passageiros;
        this.asteroides = asteroides;
        this.inimigos = inimigos;
        this.pontuacaoInicial = pontuacaoInicial;
    }

    public int passageiros() {
        return passageiros;
    }

    public int asteroides() {
        return asteroides;
    }

    public int inimigos() {
        return inimigos;
    }

    public int pontuacaoInicial() {
        return pontuacaoInicial;
    }

    /** Aceita "facil", "fácil", "FACIL"...; qualquer outro valor vira MEDIO, como no original. */
    public static Dificuldade deTexto(String texto) {
        if (texto == null) {
            return MEDIO;
        }
        return switch (texto.trim().toLowerCase()) {
            case "facil", "fácil" -> FACIL;
            case "dificil", "difícil" -> DIFICIL;
            default -> MEDIO;
        };
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
