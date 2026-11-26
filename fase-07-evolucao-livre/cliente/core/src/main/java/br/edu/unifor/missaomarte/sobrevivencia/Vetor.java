package br.edu.unifor.missaomarte.sobrevivencia;

/** Vetor 2D imutável do domínio (não usa o Vector2 do libGDX, para o domínio não depender dele). */
public record Vetor(float x, float y) {
    public static final Vetor ZERO = new Vetor(0, 0);

    public Vetor mais(Vetor o) {
        return new Vetor(x + o.x, y + o.y);
    }

    public Vetor menos(Vetor o) {
        return new Vetor(x - o.x, y - o.y);
    }

    public Vetor vezes(float k) {
        return new Vetor(x * k, y * k);
    }

    public float comprimento() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float distancia(Vetor o) {
        return menos(o).comprimento();
    }

    /** Vetor de comprimento 1 na mesma direção (ou ZERO se for nulo). */
    public Vetor normalizado() {
        float c = comprimento();
        return c < 1e-6f ? ZERO : new Vetor(x / c, y / c);
    }

    public Vetor rotacionado(float radianos) {
        float cos = (float) Math.cos(radianos);
        float sen = (float) Math.sin(radianos);
        return new Vetor(x * cos - y * sen, x * sen + y * cos);
    }

    public static Vetor polar(float angulo, float raio) {
        return new Vetor((float) Math.cos(angulo) * raio, (float) Math.sin(angulo) * raio);
    }
}
