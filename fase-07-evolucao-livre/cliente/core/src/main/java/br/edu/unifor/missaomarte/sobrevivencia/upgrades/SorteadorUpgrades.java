package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Sorteia opções distintas entre os upgrades disponíveis no momento. */
public class SorteadorUpgrades {
    private final Random random;
    private final List<Upgrade> catalogo = new ArrayList<>();

    public SorteadorUpgrades(Random random) {
        this.random = random;
        Collections.addAll(catalogo, UpgradeAtributo.values());
        catalogo.add(new UpgradeArma<>(TiroAutomatico.class, TiroAutomatico::new, "Mais projéteis em leque"));
        catalogo.add(new UpgradeArma<>(Orbital.class, Orbital::new, "Esferas giram em volta da nave"));
        catalogo.add(new UpgradeArma<>(OndaDeChoque.class, OndaDeChoque::new, "Pulso de dano em área"));
    }

    public List<OpcaoUpgrade> sortear(PartidaSobrevivencia partida, int quantidade) {
        List<Upgrade> disponiveis = new ArrayList<>();
        for (Upgrade u : catalogo) {
            if (u.disponivel(partida)) {
                disponiveis.add(u);
            }
        }
        Collections.shuffle(disponiveis, random);
        List<OpcaoUpgrade> opcoes = new ArrayList<>();
        for (Upgrade u : disponiveis.subList(0, Math.min(quantidade, disponiveis.size()))) {
            opcoes.add(new OpcaoUpgrade(u, Raridade.sortear(random)));
        }
        return List.copyOf(opcoes);
    }

}
