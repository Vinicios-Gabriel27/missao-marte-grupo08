package br.edu.unifor.missaomarte.sobrevivencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Faz os passageiros aparecerem ao longo da partida, longe o bastante da nave. */
public interface CronogramaPassageiros {
    List<Passageiro> gerar(float tempoPartida, Vetor posicaoNave, Arena arena);

    int total();

    static CronogramaPassageiros nenhum() {
        return new CronogramaPassageiros() {
            public List<Passageiro> gerar(float t, Vetor p, Arena a) {
                return List.of();
            }

            public int total() {
                return 0;
            }
        };
    }

    static CronogramaPassageiros padrao(Random random) {
        return new Escalonado(random, new float[] {10, 60, 110, 160, 210});
    }

    final class Escalonado implements CronogramaPassageiros {
        private static final String[] NOMES = {"Dr. Silva", "Eng. Rosa", "Ast. Maria", "Dr. Lima", "Eng. Carlos"};

        private final Random random;
        private final float[] instantes;
        private int proximo;

        Escalonado(Random random, float[] instantes) {
            this.random = random;
            this.instantes = instantes;
        }

        @Override
        public List<Passageiro> gerar(float tempo, Vetor nave, Arena arena) {
            List<Passageiro> novos = new ArrayList<>();
            while (proximo < instantes.length && tempo >= instantes[proximo]) {
                float angulo = random.nextFloat() * (float) (2 * Math.PI);
                float dist = 10f + random.nextFloat() * 12f;
                Vetor pos = arena.limitar(nave.mais(Vetor.polar(angulo, dist)));
                String nome = NOMES[proximo % NOMES.length];
                novos.add(switch (random.nextInt(3)) {
                    case 0 -> new Professor(nome, pos);
                    case 1 -> new Engenheiro(nome, pos);
                    default -> new Astronauta(nome, pos);
                });
                proximo++;
            }
            return novos;
        }

        @Override
        public int total() {
            return instantes.length;
        }
    }
}
