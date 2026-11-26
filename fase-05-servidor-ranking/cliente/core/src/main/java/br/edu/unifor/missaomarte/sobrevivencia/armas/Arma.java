package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;

/** Strategy: cada arma decide sozinha quando e como atacar a cada quadro. */
public interface Arma {
    String nome();

    int nivel();

    int nivelMaximo();

    void subirNivel();

    void atualizar(float delta, CampoDeBatalha campo);

    default boolean noNivelMaximo() {
        return nivel() >= nivelMaximo();
    }
}
