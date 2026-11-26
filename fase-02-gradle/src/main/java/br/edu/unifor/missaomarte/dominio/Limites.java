package br.edu.unifor.missaomarte.dominio;

/** Área jogável. Concentra a regra "esta posição está dentro do mapa?" (Information Expert). */
public record Limites(int minX, int maxX, int minY, int maxY) {
    public Limites {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Limites inválidos");
        }
    }

    /** Mapa quadrado de -tamanho a +tamanho nos dois eixos, como no jogo original. */
    public static Limites centrado(int tamanho) {
        if (tamanho <= 0) {
            throw new IllegalArgumentException("Tamanho deve ser positivo");
        }
        return new Limites(-tamanho, tamanho, -tamanho, tamanho);
    }

    public boolean contem(Posicao p) {
        return p.x() >= minX && p.x() <= maxX && p.y() >= minY && p.y() <= maxY;
    }

    public int largura() {
        return maxX - minX + 1;
    }

    public int altura() {
        return maxY - minY + 1;
    }
}
