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
}
