package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import java.util.Optional;
import java.util.function.Supplier;

/** Ganha a arma (se ainda não tem) ou sobe o nível dela (se não estiver no máximo). */
public class UpgradeArma<T extends Arma> implements Upgrade {
    private final Class<T> tipo;
    private final Supplier<T> fabrica;
    private final String descricao;

    public UpgradeArma(Class<T> tipo, Supplier<T> fabrica, String descricao) {
        this.tipo = tipo;
        this.fabrica = fabrica;
        this.descricao = descricao;
    }

    @Override
    public String titulo() {
        return fabrica.get().nome();
    }

    public String titulo(PartidaSobrevivencia partida) {
        return partida.arma(tipo)
                .map(a -> a.nome() + " (nível " + (a.nivel() + 1) + ")")
                .orElse(fabrica.get().nome() + " (nova)");
    }

    @Override
    public String descricao() {
        return descricao;
    }

    @Override
    public boolean disponivel(PartidaSobrevivencia partida) {
        return partida.arma(tipo).map(a -> !a.noNivelMaximo()).orElse(true);
    }

    @Override
    public void aplicar(PartidaSobrevivencia partida) {
        Optional<T> atual = partida.arma(tipo);
        if (atual.isPresent()) {
            atual.get().subirNivel();
        } else {
            partida.adicionarArma(fabrica.get());
        }
    }
}
