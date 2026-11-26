package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import java.util.Random;

/**
 * Raridade de uma opção de upgrade (ideia de Megabonk). Multiplica o efeito de atributos;
 * em armas, Épico e Lendário sobem dois níveis. Sorteada por peso a cada nível.
 */
public enum Raridade {
    COMUM("Comum", 1.0f, 60),
    RARO("Raro", 1.5f, 27),
    EPICO("Épico", 2.0f, 10),
    LENDARIO("Lendário", 3.0f, 3);

    private final String rotulo;
    private final float multiplicador;
    private final int peso;

    Raridade(String rotulo, float multiplicador, int peso) {
        this.rotulo = rotulo;
        this.multiplicador = multiplicador;
        this.peso = peso;
    }

    public float multiplicador() {
        return multiplicador;
    }

    public int niveisDeArma() {
        return ordinal() >= EPICO.ordinal() ? 2 : 1;
    }

    public static Raridade sortear(Random random) {
        int total = 0;
        for (Raridade r : values()) {
            total += r.peso;
        }
        int s = random.nextInt(total);
        for (Raridade r : values()) {
            s -= r.peso;
            if (s < 0) {
                return r;
            }
        }
        return COMUM;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
