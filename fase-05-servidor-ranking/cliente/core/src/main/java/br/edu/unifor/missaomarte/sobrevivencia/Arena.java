package br.edu.unifor.missaomarte.sobrevivencia;

/** Área jogável quadrada centrada na plataforma (0,0). */
public record Arena(float meiaLargura) {
    public static final Arena PADRAO = new Arena(30f);

    public Vetor limitar(Vetor p) {
        return new Vetor(Math.max(-meiaLargura, Math.min(meiaLargura, p.x())),
                Math.max(-meiaLargura, Math.min(meiaLargura, p.y())));
    }

    public boolean contem(Vetor p) {
        return Math.abs(p.x()) <= meiaLargura && Math.abs(p.y()) <= meiaLargura;
    }
}
