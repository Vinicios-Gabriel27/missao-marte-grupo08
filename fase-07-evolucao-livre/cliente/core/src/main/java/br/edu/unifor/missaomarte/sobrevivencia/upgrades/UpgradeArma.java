package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import java.util.Optional;
import java.util.function.Supplier;

/** Ganha a arma (se ainda não tem) ou sobe o nível dela. Épico/Lendário: dois níveis. */
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

    @Override
    public String titulo(PartidaSobrevivencia partida, Raridade r) {
        int sobe = r.niveisDeArma();
        return partida.arma(tipo)
                .map(a -> a.nome() + " nv " + Math.min(a.nivelMaximo(), a.nivel() + sobe))
                .orElse(fabrica.get().nome() + (sobe > 1 ? " nv 2" : " (nova)"));
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
    public void aplicar(PartidaSobrevivencia partida, Raridade r) {
        Optional<T> atual = partida.arma(tipo);
        int niveis = r.niveisDeArma();
        if (atual.isEmpty()) {
            T nova = fabrica.get();
            partida.adicionarArma(nova);
            niveis--;
            atual = Optional.of(nova);
        }
        for (int i = 0; i < niveis; i++) {
            atual.get().subirNivel();
        }
    }
}
