package br.edu.unifor.missaomarte.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class GeradorMissaoTest {

    @Test
    void quantidadesSeguemADificuldade() {
        for (Dificuldade d : Dificuldade.values()) {
            Missao m = new GeradorMissao(new Random(3)).gerar(d, Limites.centrado(5));
            assertEquals(d.passageiros(), m.getPassageiros().size(), d.name());
            assertEquals(d.asteroides(), m.getAsteroides().size(), d.name());
            assertEquals(d.inimigos(), m.getInimigos().size(), d.name());
        }
    }

    @Test
    void mesmaSementeGeraMesmoMapa() {
        Missao a = new GeradorMissao(new Random(99)).gerar(Dificuldade.DIFICIL, Limites.centrado(5));
        Missao b = new GeradorMissao(new Random(99)).gerar(Dificuldade.DIFICIL, Limites.centrado(5));
        assertEquals(posicoes(a), posicoes(b));
    }

    @Test
    void naoSorteiaNadaNaPlataformaNemSobrepoe() {
        Missao m = new GeradorMissao(new Random(5)).gerar(Dificuldade.DIFICIL, Limites.centrado(2));
        List<Posicao> todas = posicoes(m);
        assertFalse(todas.contains(Posicao.PLATAFORMA));
        assertEquals(todas.size(), todas.stream().distinct().count());
        assertTrue(todas.stream().allMatch(m.getLimites()::contem));
    }

    private List<Posicao> posicoes(Missao m) {
        return Stream.of(m.getPassageiros(), m.getAsteroides(), m.getInimigos())
                .flatMap(List::stream).map(EntidadeMapa::getPosicao).toList();
    }
}
