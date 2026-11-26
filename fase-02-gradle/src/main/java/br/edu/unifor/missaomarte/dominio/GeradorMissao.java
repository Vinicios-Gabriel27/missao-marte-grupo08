package br.edu.unifor.missaomarte.dominio;

import java.util.Optional;
import java.util.Random;

/**
 * Cria missões novas (GRASP Creator). O Random é injetado: com a mesma semente,
 * o mesmo mapa — é isso que permite testar o sorteio.
 */
public class GeradorMissao {
    public static final int CAPACIDADE_NAVE = 5;
    private static final String[][] ELENCO = {
        {"Professor", "Dr. Silva"}, {"Engenheiro", "Eng. Rosa"}, {"Professor", "Dr. Lima"},
        {"Engenheiro", "Eng. Carlos"}, {"Astronauta", "Ast. Maria"}
    };

    private final Random random;

    public GeradorMissao(Random random) {
        this.random = random;
    }

    public Missao gerar(Dificuldade dificuldade, Limites limites) {
        Missao missao = new Missao(new Nave("A-1", Posicao.PLATAFORMA, CAPACIDADE_NAVE), limites);
        for (int i = 0; i < dificuldade.passageiros(); i++) {
            Optional<Posicao> livre = sortearPosicaoLivre(missao);
            if (livre.isEmpty()) {
                break;
            }
            missao.adicionar(criarPassageiro(i, livre.get()));
        }
        for (int i = 0; i < dificuldade.asteroides(); i++) {
            sortearPosicaoLivre(missao).ifPresent(p -> missao.adicionar(new Asteroide(p)));
        }
        for (int i = 0; i < dificuldade.inimigos(); i++) {
            sortearPosicaoLivre(missao).ifPresent(p -> missao.adicionar(new Inimigo(p)));
        }
        return missao;
    }

    private Passageiro criarPassageiro(int indice, Posicao posicao) {
        String[] dados = ELENCO[indice % ELENCO.length];
        return switch (dados[0]) {
            case "Professor" -> new Professor(dados[1], posicao);
            case "Engenheiro" -> new Engenheiro(dados[1], posicao);
            default -> new Astronauta(dados[1], posicao);
        };
    }

    private Optional<Posicao> sortearPosicaoLivre(Missao missao) {
        Limites l = missao.getLimites();
        int tentativas = Math.max(20, l.largura() * l.altura() * 2);
        for (int t = 0; t < tentativas; t++) {
            Posicao p = new Posicao(l.minX() + random.nextInt(l.largura()), l.minY() + random.nextInt(l.altura()));
            if (!missao.ocupada(p)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}
