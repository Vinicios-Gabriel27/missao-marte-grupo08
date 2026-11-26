package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import java.util.ArrayList;
import java.util.List;

/**
 * Esferas girando em volta da nave (1 a 5 por nível); dano contínuo em quem encostar.
 * Evolução "Anel de Saturno": 6 esferas maiores, órbita larga, giro rápido e dano dobrado.
 */
public class Orbital extends ArmaBase {
    public static final float RAIO_ORBITA = 2.4f;
    public static final float RAIO_ESFERA = 0.45f;
    static final float VELOCIDADE_ANGULAR = 3f;
    static final float DANO_POR_SEGUNDO = 22f;

    private float angulo;
    private float area = 1f;

    @Override
    public String nome() {
        return "Orbital";
    }

    @Override
    public String nomeEvoluido() {
        return "Anel de Saturno";
    }

    public int esferas() {
        return evoluida ? 6 : nivel;
    }

    public float raioOrbita() {
        return (evoluida ? 3.6f : RAIO_ORBITA) * area;
    }

    public float raioEsfera() {
        return (evoluida ? 0.8f : RAIO_ESFERA) * area;
    }

    /** Posição de cada esfera no mundo (a tela usa para desenhar). */
    public List<Vetor> posicoes(Vetor centro) {
        List<Vetor> lista = new ArrayList<>();
        int n = esferas();
        for (int i = 0; i < n; i++) {
            float a = angulo + (float) (2 * Math.PI * i / n);
            lista.add(centro.mais(Vetor.polar(a, raioOrbita())));
        }
        return lista;
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        area = campo.multiplicadorArea();
        float velocidade = evoluida ? 5f : VELOCIDADE_ANGULAR;
        angulo = (float) ((angulo + velocidade * delta) % (2 * Math.PI));
        float dano = DANO_POR_SEGUNDO * (evoluida ? 2f : 1f) * campo.multiplicadorDano() * delta;
        for (Vetor esfera : posicoes(campo.posicaoNave())) {
            for (Inimigo inimigo : List.copyOf(campo.inimigos())) {
                if (inimigo.posicao().distancia(esfera) <= raioEsfera() + inimigo.tipo().raio()) {
                    campo.causarDano(inimigo, dano);
                }
            }
        }
    }
}
