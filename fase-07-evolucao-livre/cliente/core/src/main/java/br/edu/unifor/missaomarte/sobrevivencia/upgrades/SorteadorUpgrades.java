package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.MisseisTeleguiados;
import br.edu.unifor.missaomarte.sobrevivencia.armas.OndaDeChoque;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.armas.RaioEmCadeia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Sorteia cartas distintas entre as disponíveis. Se alguma carta cruel (evolução) estiver
 * liberada, ela aparece garantida como primeira opção.
 */
public class SorteadorUpgrades {
    private final Random random;
    private final List<Upgrade> catalogo = new ArrayList<>();

    public SorteadorUpgrades(Random random) {
        this.random = random;
        Collections.addAll(catalogo, UpgradeAtributo.values());
        catalogo.add(new UpgradeArma<>(TiroAutomatico.class, TiroAutomatico::new, "Mais projéteis em leque", "tiro"));
        catalogo.add(new UpgradeArma<>(Orbital.class, Orbital::new, "Esferas giram em volta da nave", "orbital"));
        catalogo.add(new UpgradeArma<>(OndaDeChoque.class, OndaDeChoque::new, "Pulso de dano em área", "onda"));
        catalogo.add(new UpgradeArma<>(MisseisTeleguiados.class, MisseisTeleguiados::new, "Mísseis que perseguem e explodem", "missil"));
        catalogo.add(new UpgradeArma<>(RaioEmCadeia.class, RaioEmCadeia::new, "Raio que salta entre inimigos", "raio"));
    }

    public List<OpcaoUpgrade> sortear(PartidaSobrevivencia partida, int quantidade) {
        List<OpcaoUpgrade> opcoes = new ArrayList<>();
        for (Evolucao e : Evolucao.values()) {
            if (e.disponivel(partida) && opcoes.isEmpty()) {
                opcoes.add(new OpcaoUpgrade(e, Raridade.LENDARIO));
            }
        }
        List<Upgrade> disponiveis = new ArrayList<>();
        for (Upgrade u : catalogo) {
            if (u.disponivel(partida)) {
                disponiveis.add(u);
            }
        }
        Collections.shuffle(disponiveis, random);
        for (Upgrade u : disponiveis) {
            if (opcoes.size() >= quantidade) {
                break;
            }
            opcoes.add(new OpcaoUpgrade(u, Raridade.sortear(random)));
        }
        return List.copyOf(opcoes);
    }
}
