package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import java.util.Optional;
import java.util.function.Supplier;

/** Carta de arma: ganha a arma (nível 1) ou sobe o nível. Épico/Lendário: dois níveis. */
public class UpgradeArma<T extends Arma> implements Upgrade {
    private final Class<T> tipo;
    private final Supplier<T> fabrica;
    private final String descricao;
    private final String chave;

    public UpgradeArma(Class<T> tipo, Supplier<T> fabrica, String descricao, String chave) {
        this.tipo = tipo;
        this.fabrica = fabrica;
        this.descricao = descricao;
        this.chave = chave;
    }

    public Class<T> tipo() {
        return tipo;
    }

    @Override
    public String titulo() {
        return fabrica.get().nome();
    }

    @Override
    public String titulo(PartidaSobrevivencia partida, Raridade r) {
        return titulo();
    }

    @Override
    public String descricao() {
        return descricao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public int nivelAtual(PartidaSobrevivencia partida) {
        return partida.arma(tipo).map(Arma::nivel).orElse(0);
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
