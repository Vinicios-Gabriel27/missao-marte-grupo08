package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.AtributosNave;
import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import java.util.function.Consumer;

/** Upgrades de atributo: sempre disponíveis e acumuláveis. */
public enum UpgradeAtributo implements Upgrade {
    DANO("Dano +15%", "Todos os ataques causam mais dano", a -> a.aumentarDano(0.15f)),
    CADENCIA("Cadência +15%", "As armas disparam mais rápido", a -> a.aumentarCadencia(0.15f)),
    VELOCIDADE("Velocidade +10%", "A nave se move mais rápido", a -> a.aumentarVelocidade(0.10f)),
    VIDA_MAXIMA("Vida máxima +20", "Aumenta e recupera 20 de vida", a -> a.aumentarVidaMaxima(20)),
    IMA("Ímã +30%", "Atrai cristais de XP de mais longe", a -> a.aumentarAlcanceIma(0.30f));

    private final String titulo;
    private final String descricao;
    private final Consumer<AtributosNave> efeito;

    UpgradeAtributo(String titulo, String descricao, Consumer<AtributosNave> efeito) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.efeito = efeito;
    }

    @Override
    public String titulo() {
        return titulo;
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
    public void aplicar(PartidaSobrevivencia partida) {
        efeito.accept(partida.nave().atributos());
        if (this == VIDA_MAXIMA) {
            partida.curarNave(20);
        }
    }
}
