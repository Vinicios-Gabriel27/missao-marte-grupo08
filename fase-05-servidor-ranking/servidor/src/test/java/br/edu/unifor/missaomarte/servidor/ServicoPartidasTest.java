package br.edu.unifor.missaomarte.servidor.servico;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.unifor.missaomarte.servidor.api.Dtos.PartidaRequisicao;
import br.edu.unifor.missaomarte.servidor.dominio.NivelDificuldade;
import org.junit.jupiter.api.Test;

/** Teste de unidade da regra de plausibilidade (sem Spring). */
class ServicoPartidasTest {
    @Test
    void tetoSegueAFormulaDoJogo() {
        PartidaRequisicao r = new PartidaRequisicao(0, NivelDificuldade.MEDIO, 300, 10, 50, 5, true);
        assertThat(ServicoPartidas.pontuacaoMaxima(r)).isEqualTo(50 * 10 + 300 * 2 + 5 * 100 + 500);
    }
}
