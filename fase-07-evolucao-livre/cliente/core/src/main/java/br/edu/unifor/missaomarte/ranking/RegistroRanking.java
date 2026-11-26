package br.edu.unifor.missaomarte.ranking;

/** Uma partida no ranking. Imutável; não sabe onde é guardado. */
public record RegistroRanking(String piloto, int pontuacao, String dificuldade, int tempoSegundos, int nivel,
                              int abates, int passageiros, boolean venceu, String dataHora) {
}
