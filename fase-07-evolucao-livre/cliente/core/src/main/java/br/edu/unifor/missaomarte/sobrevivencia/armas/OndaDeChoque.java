package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import java.util.List;

/**
 * Pulso periódico em área. Cada nível reduz o intervalo e aumenta o raio.
 * Evolução "Terremoto Marciano": pulso grande a cada 1,5 s que também empurra a horda para longe.
 */
public class OndaDeChoque extends ArmaBase {
    static final float DANO_BASE = 15f;

    private float recarga;
    private float tempoDesdeUltimaOnda = Float.MAX_VALUE;
    private float area = 1f;

    @Override
    public String nome() {
        return "Onda de choque";
    }

    @Override
    public String nomeEvoluido() {
        return "Terremoto Marciano";
    }

    public float intervalo() {
        return evoluida ? 1.5f : 4f - 0.6f * (nivel - 1);
    }

    public float raio() {
        return (evoluida ? 7f : 3.5f + 0.8f * (nivel - 1)) * area;
    }

    /** Para a tela animar o anel que se expande. */
    public float tempoDesdeUltimaOnda() {
        return tempoDesdeUltimaOnda;
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        area = campo.multiplicadorArea();
        recarga -= delta;
        tempoDesdeUltimaOnda += delta;
        if (recarga <= 0) {
            campo.causarDanoEmArea(campo.posicaoNave(), raio(), DANO_BASE * (evoluida ? 1.6f : 1f) * campo.multiplicadorDano());
            if (evoluida) {
                for (Inimigo i : List.copyOf(campo.inimigos())) {
                    if (i.posicao().distancia(campo.posicaoNave()) <= raio()) {
                        campo.empurrar(i, i.posicao().menos(campo.posicaoNave()).normalizado().vezes(3f));
                    }
                }
            }
            campo.registrar(Evento.ONDA_DE_CHOQUE);
            tempoDesdeUltimaOnda = 0;
            recarga = intervalo() / campo.multiplicadorCadencia();
        }
    }
}
