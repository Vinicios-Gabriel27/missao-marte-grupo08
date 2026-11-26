package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Projetil;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;

/**
 * Arma inicial: atira no inimigo mais próximo. Níveis: 1, 2, 3, 4 e 6 projéteis em leque.
 * Evolução "Tempestade de Plasma": além do leque, rajadas de 12 tiros em 360° sem parar.
 */
public class TiroAutomatico extends ArmaBase {
    static final float DANO_BASE = 8f;
    static final float INTERVALO_BASE = 0.5f;
    static final float ALCANCE = 14f;
    static final float ABERTURA = (float) Math.toRadians(11);
    static final float INTERVALO_TEMPESTADE = 0.3f;
    public static final int TIROS_TEMPESTADE = 12;
    private static final int[] PROJETEIS_POR_NIVEL = {1, 2, 3, 4, 6};

    private float recarga;
    private float recargaTempestade;
    private float giro;

    @Override
    public String nome() {
        return "Tiro múltiplo";
    }

    @Override
    public String nomeEvoluido() {
        return "Tempestade de Plasma";
    }

    public int projeteis() {
        return PROJETEIS_POR_NIVEL[nivel - 1];
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        recarga -= delta;
        if (evoluida) {
            recargaTempestade -= delta;
            giro += delta * 1.7f;
            if (recargaTempestade <= 0) {
                for (int i = 0; i < TIROS_TEMPESTADE; i++) {
                    float a = giro + (float) (2 * Math.PI * i / TIROS_TEMPESTADE);
                    campo.disparar(new Projetil(campo.posicaoNave(), Vetor.polar(a, 1f), DANO_BASE * 0.8f * campo.multiplicadorDano()));
                }
                recargaTempestade = INTERVALO_TEMPESTADE / campo.multiplicadorCadencia();
            }
        }
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
            campo.registrar(Evento.TIRO);
            recarga = INTERVALO_BASE / campo.multiplicadorCadencia();
        });
    }
}
