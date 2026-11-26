package br.edu.unifor.missaomarte.sobrevivencia;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Investida: a nave dispara na direção em que está virada, atravessa a horda causando dano
 * a quem estiver no caminho e fica invulnerável durante a arrancada e um instante depois.
 */
public class Investida implements PoderEspecial {
    public static final float DISTANCIA = 7f;
    public static final float DURACAO = 0.2f;
    public static final float RECARGA = 6f;
    static final float INVULNERAVEL_DEPOIS = 0.35f;
    static final float DANO_BASE = 35f;
    static final float RAIO_DANO = 1.5f;

    private float recarga;
    private float tempoEmAcao;
    private Vetor direcao = Vetor.ZERO;
    private final Set<Inimigo> atingidos = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public String nome() {
        return "Investida";
    }

    @Override
    public float recargaTotal() {
        return RECARGA;
    }

    @Override
    public float recargaRestante() {
        return recarga;
    }

    @Override
    public boolean emAcao() {
        return tempoEmAcao > 0;
    }

    @Override
    public void ativar(PartidaSobrevivencia partida) {
        if (!pronto()) {
            return;
        }
        direcao = partida.nave().direcao().normalizado();
        tempoEmAcao = DURACAO;
        recarga = RECARGA;
        atingidos.clear();
        partida.nave().ficarInvulneravel(DURACAO + INVULNERAVEL_DEPOIS);
        partida.registrar(Evento.PODER_ATIVADO);
    }

    @Override
    public void atualizar(float delta, PartidaSobrevivencia partida) {
        recarga = Math.max(0, recarga - delta);
        if (tempoEmAcao <= 0) {
            return;
        }
        float passo = Math.min(delta, tempoEmAcao);
        tempoEmAcao -= passo;
        partida.nave().deslocar(direcao.vezes(DISTANCIA / DURACAO * passo), partida.arena());
        Vetor p = partida.nave().posicao();
        for (Inimigo i : partida.inimigos()) {
            if (!i.morto() && !atingidos.contains(i) && i.posicao().distancia(p) <= RAIO_DANO + i.tipo().raio()) {
                atingidos.add(i);
                partida.causarDano(i, DANO_BASE * partida.multiplicadorDano());
            }
        }
    }
}
