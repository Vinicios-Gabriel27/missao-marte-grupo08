package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;

/** Opção mostrada ao subir de nível. */
public interface Upgrade {
    String titulo();

    /** Título com o valor real para a raridade (ex.: "Dano +23%"). */
    default String titulo(PartidaSobrevivencia partida, Raridade raridade) {
        return titulo();
    }

    String descricao();

    boolean disponivel(PartidaSobrevivencia partida);

    void aplicar(PartidaSobrevivencia partida, Raridade raridade);

    default void aplicar(PartidaSobrevivencia partida) {
        aplicar(partida, Raridade.COMUM);
    }
}
