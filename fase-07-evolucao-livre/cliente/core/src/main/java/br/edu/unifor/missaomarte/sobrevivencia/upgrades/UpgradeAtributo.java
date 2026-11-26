package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.AtributosNave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import java.util.function.BiConsumer;

/** Upgrades de atributo: sempre disponíveis, acumuláveis e escalados pela raridade. */
public enum UpgradeAtributo implements Upgrade {
    DANO("Dano", "Todos os ataques causam mais dano", 0.15f, true, AtributosNave::aumentarDano),
    CADENCIA("Cadência", "As armas disparam mais rápido", 0.15f, true, AtributosNave::aumentarCadencia),
    VELOCIDADE("Velocidade", "A nave se move mais rápido", 0.10f, true, AtributosNave::aumentarVelocidade),
    VIDA_MAXIMA("Vida máxima", "Aumenta e recupera vida", 20f, false, AtributosNave::aumentarVidaMaxima),
    IMA("Ímã", "Atrai cristais de XP de mais longe", 0.30f, true, AtributosNave::aumentarAlcanceIma);

    private final String nome;
    private final String descricao;
    private final float valorBase;
    private final boolean percentual;
    private final BiConsumer<AtributosNave, Float> efeito;

    UpgradeAtributo(String nome, String descricao, float valorBase, boolean percentual,
                    BiConsumer<AtributosNave, Float> efeito) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorBase = valorBase;
        this.percentual = percentual;
        this.efeito = efeito;
    }

    public float valor(Raridade r) {
        return valorBase * r.multiplicador();
    }

    @Override
    public String titulo() {
        return titulo(null, Raridade.COMUM);
    }

    @Override
    public String titulo(PartidaSobrevivencia partida, Raridade r) {
        float v = valor(r);
        return nome + (percentual ? String.format(" +%d%%", Math.round(v * 100)) : String.format(" +%d", Math.round(v)));
    }

    @Override
    public String descricao() {
        return descricao;
    }

    @Override
    public boolean disponivel(PartidaSobrevivencia partida) {
        return true;
    }

    @Override
    public void aplicar(PartidaSobrevivencia partida, Raridade r) {
        efeito.accept(partida.nave().atributos(), valor(r));
        if (this == VIDA_MAXIMA) {
            partida.curarNave(valor(r));
        }
    }
}
