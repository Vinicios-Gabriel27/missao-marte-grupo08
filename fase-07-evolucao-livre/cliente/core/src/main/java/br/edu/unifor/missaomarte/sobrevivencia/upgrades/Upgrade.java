package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;

/** Carta oferecida ao subir de nível. */
public interface Upgrade {
    String titulo();

    /** Título com o valor real para a raridade (ex.: "Dano +23%"). */
    default String titulo(PartidaSobrevivencia partida, Raridade raridade) {
        return titulo();
    }

    String descricao();

    /** Identificador do ícone da carta (a tela escolhe a imagem por ele). */
    String chave();

    /** Nível atual da carta nesta partida (0 = ainda não tem). */
    int nivelAtual(PartidaSobrevivencia partida);

    default int nivelMaximo() {
        return 5;
    }

    boolean disponivel(PartidaSobrevivencia partida);

    void aplicar(PartidaSobrevivencia partida, Raridade raridade);

    default void aplicar(PartidaSobrevivencia partida) {
        aplicar(partida, Raridade.COMUM);
    }
}
