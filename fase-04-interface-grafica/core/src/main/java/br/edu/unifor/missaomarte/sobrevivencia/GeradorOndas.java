package br.edu.unifor.missaomarte.sobrevivencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Decide quando e quais inimigos entram. É uma interface para a Partida não depender
 * de uma regra fixa de ondas (DIP): os testes usam {@link #nenhum()} e controlam tudo à mão.
 */
public interface GeradorOndas {
    List<Inimigo> gerar(float tempoPartida, float delta, Vetor posicaoNave, Arena arena);

    static GeradorOndas nenhum() {
        return (t, d, p, a) -> List.of();
    }

    static GeradorOndas padrao(Random random, float multiplicador) {
        return new OndasCrescentes(random, multiplicador);
    }

    /** Ondas que ficam maiores e mais frequentes a cada 30 s. */
    final class OndasCrescentes implements GeradorOndas {
        static final float DISTANCIA_MIN = 16f;
        static final float DISTANCIA_MAX = 21f;

        private final Random random;
        private final float multiplicador;
        private float recarga = 1f;

        OndasCrescentes(Random random, float multiplicador) {
            this.random = random;
            this.multiplicador = multiplicador;
        }

        @Override
        public List<Inimigo> gerar(float tempo, float delta, Vetor nave, Arena arena) {
            recarga -= delta;
            if (recarga > 0) {
                return List.of();
            }
            int fase = (int) (tempo / 30f);
            recarga = Math.max(0.5f, 1.8f - 0.1f * fase);
            int quantidade = Math.max(1, Math.round((1 + fase * 0.5f) * multiplicador));
            List<Inimigo> novos = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                float angulo = random.nextFloat() * (float) (2 * Math.PI);
                float dist = DISTANCIA_MIN + random.nextFloat() * (DISTANCIA_MAX - DISTANCIA_MIN);
                novos.add(new Inimigo(sortearTipo(tempo), arena.limitar(nave.mais(Vetor.polar(angulo, dist)))));
            }
            return novos;
        }

        private TipoInimigo sortearTipo(float tempo) {
            int pesoRapido = tempo >= TipoInimigo.RAPIDO.surgeAPartirDe() ? 3 : 0;
            int pesoTanque = tempo >= TipoInimigo.TANQUE.surgeAPartirDe() ? 1 : 0;
            int sorteio = random.nextInt(6 + pesoRapido + pesoTanque);
            if (sorteio < 6) {
                return TipoInimigo.PERSEGUIDOR;
            }
            return sorteio < 6 + pesoRapido ? TipoInimigo.RAPIDO : TipoInimigo.TANQUE;
        }
    }
}
