package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Projetil;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;

/**
 * Arma inicial: atira sozinha no inimigo mais próximo. O upgrade "Tiro múltiplo"
 * sobe o nível dela: 1, 2, 3 e 5 projéteis em leque.
 */
public class TiroAutomatico implements Arma {
    static final float DANO_BASE = 8f;
    static final float INTERVALO_BASE = 0.5f;
    static final float ALCANCE = 14f;
    static final float ABERTURA = (float) Math.toRadians(12);
    private static final int[] PROJETEIS_POR_NIVEL = {1, 2, 3, 5};

    private int nivel = 1;
    private float recarga;

    @Override
    public String nome() {
        return "Tiro múltiplo";
    }

    @Override
    public int nivel() {
        return nivel;
    }

    @Override
    public int nivelMaximo() {
        return PROJETEIS_POR_NIVEL.length;
    }

    @Override
    public void subirNivel() {
        if (!noNivelMaximo()) {
            nivel++;
        }
    }

    public int projeteis() {
        return PROJETEIS_POR_NIVEL[nivel - 1];
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        recarga -= delta;
        if (recarga > 0) {
            return;
        }
        campo.inimigoMaisProximo(ALCANCE).ifPresent(alvo -> {
            Vetor origem = campo.posicaoNave();
            Vetor direcao = alvo.posicao().menos(origem).normalizado();
            int n = projeteis();
            for (int i = 0; i < n; i++) {
                float desvio = (i - (n - 1) / 2f) * ABERTURA;
                campo.disparar(new Projetil(origem, direcao.rotacionado(desvio), DANO_BASE * campo.multiplicadorDano()));
            }
            campo.registrar(br.edu.unifor.missaomarte.sobrevivencia.Evento.TIRO);
            recarga = INTERVALO_BASE / campo.multiplicadorCadencia();
        });
    }
}
