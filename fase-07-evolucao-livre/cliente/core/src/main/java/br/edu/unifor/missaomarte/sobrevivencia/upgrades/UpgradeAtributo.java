package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.AtributosNave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import java.util.function.BiConsumer;

/** Cartas passivas (nível 1 a 5), escaladas pela raridade. */
public enum UpgradeAtributo implements Upgrade {
    DANO("Dano", "Todos os ataques causam mais dano", 0.15f, true, AtributosNave::aumentarDano),
    CADENCIA("Cadência", "As armas disparam mais rápido", 0.15f, true, AtributosNave::aumentarCadencia),
    VELOCIDADE("Velocidade", "A nave voa mais rápido", 0.10f, true, AtributosNave::aumentarVelocidade),
    VIDA_MAXIMA("Vida máxima", "Aumenta a vida máxima e cura na hora", 20f, false, AtributosNave::aumentarVidaMaxima),
    IMA("Ímã", "Atrai cristais de mais longe", 0.30f, true, AtributosNave::aumentarAlcanceIma),
    ARMADURA("Armadura", "Bloqueia parte do dano recebido", 0.08f, true, AtributosNave::aumentarArmadura),
    AREA("Área", "Orbitais, ondas e explosões maiores", 0.15f, true, AtributosNave::aumentarArea),
    CRITICO("Crítico", "Chance de causar dano dobrado", 0.08f, true, AtributosNave::aumentarCritico);

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

    public String nome() {
        return nome;
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
    public String chave() {
        return name().toLowerCase();
    }

    @Override
    public int nivelAtual(PartidaSobrevivencia partida) {
        return partida.nivelPassiva(this);
    }

    @Override
    public boolean disponivel(PartidaSobrevivencia partida) {
        return partida.nivelPassiva(this) < nivelMaximo();
    }

    @Override
    public void aplicar(PartidaSobrevivencia partida, Raridade r) {
        efeito.accept(partida.nave().atributos(), valor(r));
        partida.subirPassiva(this);
        if (this == VIDA_MAXIMA) {
            // cura de verdade: 40% da vida máxima, escalada pela raridade (Lendário enche a barra)
            float cura = partida.nave().atributos().vidaMaxima() * 0.4f * r.multiplicador();
            partida.curarNave(cura);
            partida.registrar(br.edu.unifor.missaomarte.sobrevivencia.Evento.NAVE_CURADA);
        }
    }
}
