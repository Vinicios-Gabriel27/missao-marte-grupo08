package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import java.util.ArrayList;
import java.util.List;

/** Esferas girando em volta da nave; causam dano contínuo em quem encostar. */
public class Orbital implements Arma {
    public static final float RAIO_ORBITA = 2.4f;
    public static final float RAIO_ESFERA = 0.45f;
    static final float VELOCIDADE_ANGULAR = 3f;
    static final float DANO_POR_SEGUNDO = 22f;

    private int nivel = 1;
    private float angulo;

    @Override
    public String nome() {
        return "Orbital";
    }

    @Override
    public int nivel() {
        return nivel;
    }

    @Override
    public int nivelMaximo() {
        return 3;
    }

    @Override
    public void subirNivel() {
        if (!noNivelMaximo()) {
            nivel++;
        }
    }

    /** Posição de cada esfera no mundo (a tela usa para desenhar). */
    public List<Vetor> posicoes(Vetor centro) {
        List<Vetor> lista = new ArrayList<>();
        for (int i = 0; i < nivel; i++) {
            float a = angulo + (float) (2 * Math.PI * i / nivel);
            lista.add(centro.mais(Vetor.polar(a, RAIO_ORBITA)));
        }
        return lista;
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        angulo = (float) ((angulo + VELOCIDADE_ANGULAR * delta) % (2 * Math.PI));
        float dano = DANO_POR_SEGUNDO * campo.multiplicadorDano() * delta;
        for (Vetor esfera : posicoes(campo.posicaoNave())) {
            for (Inimigo inimigo : List.copyOf(campo.inimigos())) {
                if (inimigo.posicao().distancia(esfera) <= RAIO_ESFERA + inimigo.tipo().raio()) {
                    campo.causarDano(inimigo, dano);
                }
            }
        }
    }
}
