package br.edu.unifor.missaomarte.sobrevivencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unifor.missaomarte.sobrevivencia.armas.Arma;
import br.edu.unifor.missaomarte.sobrevivencia.armas.MisseisTeleguiados;
import br.edu.unifor.missaomarte.sobrevivencia.armas.Orbital;
import br.edu.unifor.missaomarte.sobrevivencia.armas.RaioEmCadeia;
import br.edu.unifor.missaomarte.sobrevivencia.armas.TiroAutomatico;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Evolucao;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.OpcaoUpgrade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.Raridade;
import br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeAtributo;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Sistema de cartas: níveis 1–5, passivas novas, armas novas e cartas cruéis (evoluções). */
class CartasTest {
    private static final float QUADRO = 1f / 60f;

    private PartidaSobrevivencia partida() {
        return new PartidaSobrevivencia(Dificuldade.MEDIO, Arena.PADRAO, new Random(5), GeradorOndas.nenhum(),
                CronogramaPassageiros.nenhum());
    }

    private void maximizar(Arma a) {
        while (!a.noNivelMaximo()) {
            a.subirNivel();
        }
    }

    @Test
    void passivaVaiAteONivelCinco() {
        PartidaSobrevivencia p = partida();
        for (int i = 0; i < 5; i++) {
            assertTrue(UpgradeAtributo.AREA.disponivel(p));
            UpgradeAtributo.AREA.aplicar(p);
        }
        assertEquals(5, p.nivelPassiva(UpgradeAtributo.AREA));
        assertFalse(UpgradeAtributo.AREA.disponivel(p));
    }

    @Test
    void armaduraReduzODanoRecebido() {
        PartidaSobrevivencia p = partida();
        new OpcaoUpgrade(UpgradeAtributo.ARMADURA, Raridade.LENDARIO).aplicar(p); // 24%
        float antes = p.nave().vida();
        p.adicionarInimigo(new Inimigo(TipoInimigo.TANQUE, new Vetor(0.5f, 0)));
        p.atualizar(QUADRO, Vetor.ZERO);
        assertEquals(antes - TipoInimigo.TANQUE.danoContato() * (1 - 0.24f), p.nave().vida(), 0.01f);
    }

    @Test
    void criticoDobraODano() {
        PartidaSobrevivencia p = partida();
        for (int i = 0; i < 4; i++) {
            new OpcaoUpgrade(UpgradeAtributo.CRITICO, Raridade.LENDARIO).aplicar(p); // limitado a 75%
        }
        int criticos = 0;
        for (int i = 0; i < 400; i++) {
            Inimigo alvo = new Inimigo(TipoInimigo.CHEFE, new Vetor(25, 25));
            p.causarDano(alvo, 10f);
            if (alvo.vida() == TipoInimigo.CHEFE.vida() - 20f) {
                criticos++;
            }
        }
        assertEquals(0.75, criticos / 400.0, 0.08);
    }

    @Test
    void areaAumentaAOrbita() {
        PartidaSobrevivencia p = partida();
        Orbital o = new Orbital();
        p.adicionarArma(o);
        p.atualizar(QUADRO, Vetor.ZERO);
        float antes = o.raioOrbita();
        new OpcaoUpgrade(UpgradeAtributo.AREA, Raridade.EPICO).aplicar(p);
        p.atualizar(QUADRO, Vetor.ZERO);
        assertEquals(antes * 1.3f, o.raioOrbita(), 0.01f);
    }

    @Test
    void misseisPerseguemEExplodemNoAlvo() {
        PartidaSobrevivencia p = partida();
        p.adicionarArma(new MisseisTeleguiados());
        Inimigo alvo = new Inimigo(TipoInimigo.TANQUE, new Vetor(-6, 4));
        p.adicionarInimigo(alvo);
        for (int i = 0; i < 120; i++) {
            p.atualizar(QUADRO, Vetor.ZERO);
        }
        assertTrue(alvo.vida() < TipoInimigo.TANQUE.vida() - 15f);
    }

    @Test
    void raioSaltaEntreVariosInimigos() {
        PartidaSobrevivencia p = partida();
        RaioEmCadeia raio = new RaioEmCadeia();
        p.adicionarArma(raio);
        for (int i = 0; i < 5; i++) {
            p.adicionarInimigo(new Inimigo(TipoInimigo.TANQUE, new Vetor(4 + i * 2.5f, 0)));
        }
        for (int i = 0; i < 30; i++) {
            p.atualizar(QUADRO, Vetor.ZERO);
        }
        assertEquals(1 + raio.alvosPorDisparo(), raio.ultimoCaminho().size(), "nave + 3 alvos no nível 1");
    }

    @Test
    void cartaCruelSoApareceComArmaNoMaximoEPassivaCerta() {
        PartidaSobrevivencia p = partida();
        Arma tiro = p.arma(TiroAutomatico.class).orElseThrow();
        assertFalse(Evolucao.TEMPESTADE_DE_PLASMA.disponivel(p));
        maximizar(tiro);
        assertFalse(Evolucao.TEMPESTADE_DE_PLASMA.disponivel(p), "falta a passiva Cadência");
        UpgradeAtributo.CADENCIA.aplicar(p);
        assertTrue(Evolucao.TEMPESTADE_DE_PLASMA.disponivel(p));
    }

    @Test
    void cartaCruelVemGarantidaNasOpcoesEEvoluiAArma() {
        PartidaSobrevivencia p = partida();
        Arma tiro = p.arma(TiroAutomatico.class).orElseThrow();
        maximizar(tiro);
        UpgradeAtributo.CADENCIA.aplicar(p);
        p.adicionarCristal(new CristalXp(Vetor.ZERO, p.xpParaProximoNivel()));
        p.atualizar(QUADRO, Vetor.ZERO);
        List<OpcaoUpgrade> opcoes = p.opcoesUpgrade();
        assertTrue(opcoes.get(0).ehEvolucao());
        assertEquals(3, opcoes.size());

        p.escolherUpgrade(0);
        assertTrue(tiro.evoluida());
        assertEquals("Tempestade de Plasma", tiro.nomeAtual());
        assertFalse(Evolucao.TEMPESTADE_DE_PLASMA.disponivel(p), "cada carta cruel só uma vez");
        assertEquals(List.of(Evolucao.TEMPESTADE_DE_PLASMA), p.evolucoes());
    }

    @Test
    void tempestadeDePlasmaDisparaEm360Graus() {
        PartidaSobrevivencia p = partida();
        TiroAutomatico tiro = p.arma(TiroAutomatico.class).orElseThrow();
        tiro.evoluir();
        p.atualizar(QUADRO, Vetor.ZERO); // sem inimigos: só a rajada 360°
        assertEquals(TiroAutomatico.TIROS_TEMPESTADE, p.projeteis().size());
    }

    @Test
    void anelDeSaturnoTemSeisEsferasMaiores() {
        Orbital o = new Orbital();
        float raioAntes = o.raioEsfera();
        o.evoluir();
        assertEquals(6, o.esferas());
        assertTrue(o.raioEsfera() > raioAntes);
    }

    @Test
    void cartasMostramNivelAntesEDepois() {
        PartidaSobrevivencia p = partida();
        OpcaoUpgrade dano = new OpcaoUpgrade(UpgradeAtributo.DANO, Raridade.COMUM);
        assertEquals(0, dano.nivelAtual(p));
        assertEquals(1, dano.nivelDepois(p));
        OpcaoUpgrade tiroEpico = new OpcaoUpgrade(
                new br.edu.unifor.missaomarte.sobrevivencia.upgrades.UpgradeArma<>(TiroAutomatico.class, TiroAutomatico::new, "", "tiro"),
                Raridade.EPICO);
        assertEquals(1, tiroEpico.nivelAtual(p));
        assertEquals(3, tiroEpico.nivelDepois(p));
    }
}
