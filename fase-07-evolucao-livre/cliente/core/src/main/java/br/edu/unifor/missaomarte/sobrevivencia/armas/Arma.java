package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;

/**
 * Strategy: cada arma decide sozinha quando e como atacar a cada quadro.
 * Todas vão do nível 1 ao 5 e podem virar uma "carta cruel" (evolução) com a passiva certa.
 */
public interface Arma {
    int NIVEL_MAXIMO = 5;

    String nome();

    /** Nome depois de evoluir (carta cruel). */
    String nomeEvoluido();

    int nivel();

    default int nivelMaximo() {
        return NIVEL_MAXIMO;
    }

    void subirNivel();

    boolean evoluida();

    void evoluir();

    void atualizar(float delta, CampoDeBatalha campo);

    default boolean noNivelMaximo() {
        return nivel() >= nivelMaximo();
    }

    default String nomeAtual() {
        return evoluida() ? nomeEvoluido() : nome();
    }
}
