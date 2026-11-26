package br.edu.unifor.missaomarte.sobrevivencia.armas;

import br.edu.unifor.missaomarte.sobrevivencia.CampoDeBatalha;
import br.edu.unifor.missaomarte.sobrevivencia.Evento;
import br.edu.unifor.missaomarte.sobrevivencia.Inimigo;
import br.edu.unifor.missaomarte.sobrevivencia.Vetor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Raio que acerta o inimigo mais próximo e salta para os vizinhos (3 a 7 alvos por nível).
 * Evolução "Tempestade Elétrica": dispara a cada 0,6 s e salta por até 14 inimigos.
 */
public class RaioEmCadeia extends ArmaBase {
    static final float INTERVALO = 1.8f;
    static final float ALCANCE = 10f;
    static final float SALTO = 4.5f;
    static final float DANO = 18f;

    private float recarga = 0.3f;
    private final List<Vetor> ultimoCaminho = new ArrayList<>();
    private float tempoDesdeDisparo = Float.MAX_VALUE;

    @Override
    public String nome() {
        return "Raio em cadeia";
    }

    @Override
    public String nomeEvoluido() {
        return "Tempestade Elétrica";
    }

    public int alvosPorDisparo() {
        return evoluida ? 14 : 2 + nivel;
    }

    /** Pontos do último raio (nave → alvos), para a tela desenhar. */
    public List<Vetor> ultimoCaminho() {
        return Collections.unmodifiableList(ultimoCaminho);
    }

    public float tempoDesdeDisparo() {
        return tempoDesdeDisparo;
    }

    @Override
    public void atualizar(float delta, CampoDeBatalha campo) {
        recarga -= delta;
        tempoDesdeDisparo += delta;
        if (recarga > 0) {
            return;
        }
        var primeiro = campo.inimigoMaisProximo(ALCANCE);
        if (primeiro.isEmpty()) {
            return;
        }
        ultimoCaminho.clear();
        ultimoCaminho.add(campo.posicaoNave());
        Set<Inimigo> atingidos = Collections.newSetFromMap(new IdentityHashMap<>());
        Inimigo atual = primeiro.get();
        float dano = DANO * (evoluida ? 1.4f : 1f) * campo.multiplicadorDano();
        for (int salto = 0; atual != null && salto < alvosPorDisparo(); salto++) {
            atingidos.add(atual);
            ultimoCaminho.add(atual.posicao());
            campo.causarDano(atual, dano);
            atual = vizinho(campo, atual, atingidos);
        }
        campo.registrar(Evento.RAIO_DISPARADO);
        tempoDesdeDisparo = 0;
        recarga = (evoluida ? 0.6f : INTERVALO) / campo.multiplicadorCadencia();
    }

    private static Inimigo vizinho(CampoDeBatalha campo, Inimigo de, Set<Inimigo> excluir) {
        Inimigo melhor = null;
        float d = SALTO;
        for (Inimigo i : campo.inimigos()) {
            float di = i.posicao().distancia(de.posicao());
            if (!excluir.contains(i) && !i.morto() && di <= d) {
                d = di;
                melhor = i;
            }
        }
        return melhor;
    }
}
