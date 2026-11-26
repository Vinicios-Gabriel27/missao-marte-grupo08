package br.edu.unifor.missaomarte.sobrevivencia;

import java.util.List;
import java.util.Optional;

/**
 * O que uma arma pode ver e fazer no jogo (ISP): ela não recebe a Partida inteira,
 * só estas operações. Assim uma arma nova não depende de detalhes do laço do jogo.
 */
public interface CampoDeBatalha {
    Vetor posicaoNave();

    Vetor direcaoNave();

    float multiplicadorDano();

    float multiplicadorCadencia();

    Optional<Inimigo> inimigoMaisProximo(float alcance);

    List<Inimigo> inimigos();

    void disparar(Projetil projetil);

    void causarDano(Inimigo inimigo, float dano);

    void causarDanoEmArea(Vetor centro, float raio, float dano);

    void registrar(Evento evento);
}
