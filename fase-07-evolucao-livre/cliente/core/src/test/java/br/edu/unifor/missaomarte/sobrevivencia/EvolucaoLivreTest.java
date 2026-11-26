package br.edu.unifor.missaomarte.sobrevivencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.OpcaoUpgrade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Raridade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeAtributo;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Fase 7 — raridade dos upgrades, poder especial (Investida) e padrões de ataque do chefe. */
class EvolucaoLivreTest {
    private static final float QUADRO = 1f / 60f;

    private PartidaSobrevivencia partida() {
        return new PartidaSobrevivencia(Dificuldade.MEDIO, Arena.PADRAO, new Random(3), GeradorOndas.nenhum(),
                CronogramaPassageiros.nenhum());
    }

    // ---------- raridade ----------

    @Test
    void raridadeSegueOsPesos() {
        Random r = new Random(11);
        Map<Raridade, Integer> conta = new EnumMap<>(Raridade.class);
        for (int i = 0; i < 20_000; i++) {
            conta.merge(Raridade.sortear(r), 1, Integer::sum);
        }
        assertTrue(conta.get(Raridade.COMUM) > conta.get(Raridade.RARO));
        assertTrue(conta.get(Raridade.RARO) > conta.get(Raridade.EPICO));
        assertTrue(conta.get(Raridade.EPICO) > conta.get(Raridade.LENDARIO));
        assertEquals(0.03, conta.get(Raridade.LENDARIO) / 20_000.0, 0.01);
    }

    @Test
    void lendarioTriplicaOAtributo() {
        PartidaSobrevivencia p = partida();
        float antes = p.nave().atributos().multiplicadorDano();
        new OpcaoUpgrade(UpgradeAtributo.DANO, Raridade.LENDARIO).aplicar(p);
        assertEquals(antes + 0.45f, p.nave().atributos().multiplicadorDano(), 1e-5f);
        assertEquals("Dano +45%", UpgradeAtributo.DANO.titulo(p, Raridade.LENDARIO));
    }

    @Test
    void armaEpicaSobeDoisNiveis() {
        PartidaSobrevivencia p = partida();
        new OpcaoUpgrade(new UpgradeArma<>(Orbital.class, Orbital::new, "", "orbital"), Raridade.EPICO).aplicar(p);
        assertEquals(2, p.arma(Orbital.class).orElseThrow().nivel());
    }

    // ---------- investida ----------

    @Test
    void investidaAvancaNaDirecaoDaNaveEEntraEmRecarga() {
        PartidaSobrevivencia p = partida();
        p.atualizar(QUADRO, new Vetor(1, 0)); // vira para a direita
        float x0 = p.nave().posicao().x();
        p.ativarPoder();
        List<Evento> eventos = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            eventos.addAll(p.atualizar(QUADRO, Vetor.ZERO));
        }
        assertTrue(eventos.contains(Evento.PODER_ATIVADO));
        assertEquals(x0 + Investida.DISTANCIA, p.nave().posicao().x(), 0.3f);
        assertFalse(p.poder().pronto());
        p.ativarPoder(); // ainda recarregando: não faz nada
        float x1 = p.nave().posicao().x();
        p.atualizar(QUADRO, Vetor.ZERO);
        assertEquals(x1, p.nave().posicao().x(), 1e-4f);
    }

    @Test
    void investidaFicaProntaDepoisDaRecarga() {
        PartidaSobrevivencia p = partida();
        p.ativarPoder();
        for (float t = 0; t < Investida.RECARGA + 0.1f; t += 0.1f) {
            p.atualizar(0.1f, Vetor.ZERO);
        }
        assertTrue(p.poder().pronto());
    }

    @Test
    void investidaFereInimigosNoCaminhoESemLevarDano() {
        PartidaSobrevivencia p = partida();
        p.atualizar(QUADRO, new Vetor(1, 0));
        Inimigo tanque = new Inimigo(TipoInimigo.TANQUE, new Vetor(3.5f, 0));
        p.adicionarInimigo(tanque);
        float vida = p.nave().vida();
        p.ativarPoder();
        for (int i = 0; i < 15; i++) {
            p.atualizar(QUADRO, Vetor.ZERO);
        }
        assertTrue(tanque.vida() < TipoInimigo.TANQUE.vida() - 30f);
        assertEquals(vida, p.nave().vida(), 1e-4f, "invulnerável durante a investida");
    }

    // ---------- chefe ----------

    /** Dá muita vida à nave para observar o chefe sem ela ser destruída por um anel inteiro. */
    private void blindar(PartidaSobrevivencia p) {
        for (int i = 0; i < 20; i++) {
            new OpcaoUpgrade(UpgradeAtributo.VIDA_MAXIMA, Raridade.LENDARIO).aplicar(p);
        }
    }

    private Inimigo colocarChefe(PartidaSobrevivencia p, Vetor pos) {
        blindar(p);
        Inimigo chefe = new Inimigo(TipoInimigo.CHEFE, pos);
        chefe.definirComportamento(new ComportamentoChefe());
        p.adicionarInimigo(chefe);
        return chefe;
    }

    @Test
    void chefeCumpreOCicloPerseguirMirarInvestirAnel() {
        PartidaSobrevivencia p = partida();
        Inimigo chefe = colocarChefe(p, new Vetor(15, 0));
        List<Evento> eventos = new ArrayList<>();
        List<ComportamentoChefe.Estado> vistos = new ArrayList<>();
        for (int i = 0; i < 60 * 7; i++) {
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
            eventos.addAll(p.atualizar(QUADRO, Vetor.ZERO));
            p.curarNave(1000); // mantém a nave viva para observar o ciclo inteiro
            ComportamentoChefe.Estado e = chefe.comportamento().estado();
            if (vistos.isEmpty() || vistos.get(vistos.size() - 1) != e) {
                vistos.add(e);
            }
        }
        assertEquals(List.of(ComportamentoChefe.Estado.PERSEGUIR, ComportamentoChefe.Estado.MIRAR,
                ComportamentoChefe.Estado.INVESTIR, ComportamentoChefe.Estado.ANEL, ComportamentoChefe.Estado.PERSEGUIR),
                vistos.subList(0, 5));
        assertTrue(eventos.contains(Evento.CHEFE_MIRANDO));
        assertTrue(eventos.contains(Evento.CHEFE_ANEL));
    }

    @Test
    void anelDoChefeCriaProjeteisQueFeremANave() {
        PartidaSobrevivencia p = partida();
        colocarChefe(p, new Vetor(12, 0));
        int maxProjeteis = 0;
        int acertos = 0;
        for (int i = 0; i < 60 * 8; i++) {
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
            acertos += (int) p.atualizar(QUADRO, Vetor.ZERO).stream().filter(e -> e == Evento.NAVE_ATINGIDA).count();
            p.curarNave(1000);
            maxProjeteis = Math.max(maxProjeteis, p.projeteisInimigos().size());
        }
        assertTrue(maxProjeteis >= 16, "o anel tem pelo menos 16 projéteis");
        assertTrue(acertos > 0, "algum golpe do chefe acertou a nave parada");
    }

    @Test
    void chefeFuriosoDisparaMaisProjeteis() {
        PartidaSobrevivencia p = partida();
        Inimigo chefe = colocarChefe(p, new Vetor(20, 20));
        chefe.sofrerDano(TipoInimigo.CHEFE.vida() * 0.6f);
        int maximo = 0;
        for (int i = 0; i < 60 * 6; i++) {
            if (p.estado() == EstadoPartida.ESCOLHENDO_UPGRADE) {
                p.escolherUpgrade(0);
            }
            p.atualizar(QUADRO, new Vetor(-1, -1));
            p.curarNave(1000);
            maximo = Math.max(maximo, p.projeteisInimigos().size());
        }
        assertTrue(maximo >= 36, "furioso: anel de 24 + 12");
    }
}
