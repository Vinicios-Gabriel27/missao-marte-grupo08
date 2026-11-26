package br.edu.unifor.missaomarte.sobrevivencia.upgrades;

import br.edu.unifor.missaomarte.sobrevivencia.PartidaSobrevivencia;

/** Opção mostrada ao subir de nível. */
public interface Upgrade {
    String titulo();

    String descricao();

    boolean disponivel(PartidaSobrevivencia partida);

    void aplicar(PartidaSobrevivencia partida);
}
