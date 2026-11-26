package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;

/** Uma opção mostrada ao subir de nível: o upgrade e a raridade sorteada para ele. */
public record OpcaoUpgrade(Upgrade upgrade, Raridade raridade) {
    public String titulo() {
        return upgrade.titulo();
    }

    public String titulo(PartidaSobrevivencia partida) {
        return upgrade.titulo(partida, raridade);
    }

    public String descricao() {
        return upgrade.descricao();
    }

    public void aplicar(PartidaSobrevivencia partida) {
        upgrade.aplicar(partida, raridade);
    }

    public boolean ehArma() {
        return upgrade instanceof UpgradeArma<?>;
    }

    /** Carta cruel (evolução). */
    public boolean ehEvolucao() {
        return upgrade instanceof Evolucao;
    }

    public String chave() {
        return upgrade.chave();
    }

    public int nivelAtual(PartidaSobrevivencia partida) {
        return upgrade.nivelAtual(partida);
    }

    /** Nível que a carta terá depois de escolhida. */
    public int nivelDepois(PartidaSobrevivencia partida) {
        int sobe = ehArma() ? raridade.niveisDeArma() : 1;
        return Math.min(upgrade.nivelMaximo(), upgrade.nivelAtual(partida) + sobe);
    }

    public int nivelMaximo() {
        return upgrade.nivelMaximo();
    }
}
