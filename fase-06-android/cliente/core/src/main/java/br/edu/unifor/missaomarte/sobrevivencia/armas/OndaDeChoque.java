package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;

/** Pulso periódico em área. Cada nível reduz o intervalo e aumenta o raio. */
public class OndaDeChoque implements Arma {
    static final float DANO_BASE = 15f;

    private int nivel = 1;
    private float recarga;
    private float tempoDesdeUltimaOnda = Float.MAX_VALUE;

    @Override
    public String nome() {
        return "Onda de choque";
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

    public float intervalo() {
        return 4f - 0.8f * (nivel - 1);
    }

    public float raio() {
        return 3.5f + 1f * (nivel - 1);
    }

    /** Para a tela animar o anel que se expande. */
    public float tempoDesdeUltimaOnda() {
        return tempoDesdeUltimaOnda;
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        recarga -= delta;
        tempoDesdeUltimaOnda += delta;
        if (recarga <= 0) {
            campo.causarDanoEmArea(campo.posicaoNave(), raio(), DANO_BASE * campo.multiplicadorDano());
            campo.registrar(Evento.ONDA_DE_CHOQUE);
            tempoDesdeUltimaOnda = 0;
            recarga = intervalo() / campo.multiplicadorCadencia();
        }
    }
}
