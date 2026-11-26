package br.edu.unifor.missaomarte.aplicacao;

import br.edu.unifor.missaomarte.dominio.Dificuldade;

/** Uma linha do ranking. Só dados (imutável). */
public record RegistroRanking(String piloto, int pontuacao, Dificuldade dificuldade,
                              int passageirosResgatados, long tempoSegundos, String dataHora) {
}
