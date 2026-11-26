package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Lança mísseis que perseguem o inimigo mais próximo e explodem em área.
 * Níveis: 1, 1, 2, 2, 3 mísseis por salva, com explosão crescente.
 * Evolução "Chuva de Meteoros": salvas de 5 a cada 1,2 s com explosões enormes.
 */
public class MisseisTeleguiados extends ArmaBase {
    static final float INTERVALO = 2.2f;
    static final float VELOCIDADE = 10f;
    static final float DANO = 22f;
    static final float VIDA_MISSIL = 3f;
    private static final int[] POR_SALVA = {1, 1, 2, 2, 3};

    /** Míssil em voo (a tela desenha pela posição e direção). */
    public static final class Missil {
        Vetor posicao;
        Vetor direcao;
        float vida = VIDA_MISSIL;

        public Vetor posicao() {
            return posicao;
        }

        public Vetor direcao() {
            return direcao;
        }
    }

    private final List<Missil> emVoo = new ArrayList<>();
    private float recarga = 0.5f;

    @Override
    public String nome() {
        return "Mísseis teleguiados";
    }

    @Override
    public String nomeEvoluido() {
        return "Chuva de Meteoros";
    }

    public float raioExplosao(CampoDeBatalha campo) {
        return (evoluida ? 3.6f : 1.6f + 0.2f * nivel) * campo.multiplicadorArea();
    }

    public List<Missil> emVoo() {
        return Collections.unmodifiableList(emVoo);
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        recarga -= delta;
        if (recarga <= 0 && !campo.inimigos().isEmpty()) {
            int n = evoluida ? 5 : POR_SALVA[nivel - 1];
            for (int i = 0; i < n; i++) {
                Missil m = new Missil();
                m.posicao = campo.posicaoNave();
                m.direcao = Vetor.polar((float) (2 * Math.PI * i / n) + recarga, 1f);
                emVoo.add(m);
            }
            campo.registrar(Evento.MISSIL_LANCADO);
            recarga = (evoluida ? 1.2f : INTERVALO) / campo.multiplicadorCadencia();
        }
        Iterator<Missil> it = emVoo.iterator();
        while (it.hasNext()) {
            Missil m = it.next();
            m.vida -= delta;
            Optional<Inimigo> alvo = maisProximo(campo, m.posicao);
            if (alvo.isPresent()) {
                Vetor desejada = alvo.get().posicao().menos(m.posicao).normalizado();
                m.direcao = m.direcao.vezes(1 - Math.min(1, delta * 6)).mais(desejada.vezes(Math.min(1, delta * 6))).normalizado();
            }
            m.posicao = m.posicao.mais(m.direcao.vezes(VELOCIDADE * delta));
            boolean acertou = alvo.isPresent() && alvo.get().posicao().distancia(m.posicao) <= 0.4f + alvo.get().tipo().raio();
            if (acertou || m.vida <= 0) {
                campo.causarDanoEmArea(m.posicao, raioExplosao(campo), DANO * (evoluida ? 1.8f : 1f) * campo.multiplicadorDano());
                it.remove();
            }
        }
    }

    private static Optional<Inimigo> maisProximo(CampoDeBatalha campo, Vetor de) {
        Inimigo melhor = null;
        float d = Float.MAX_VALUE;
        for (Inimigo i : campo.inimigos()) {
            float di = i.posicao().distancia(de);
            if (!i.morto() && di < d) {
                d = di;
                melhor = i;
            }
        }
        return Optional.ofNullable(melhor);
    }
}
