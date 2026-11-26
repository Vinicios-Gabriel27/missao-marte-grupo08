package br.edu.unifor.missaomarte.servidor.servico;

import br.edu.unifor.missaomarte.servidor.api.Dtos.PartidaRequisicao;
import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import br.edu.unifor.missaomarte.servidor.dominio.NivelDificuldade;
import br.edu.unifor.missaomarte.servidor.dominio.PartidaRegistrada;
import br.edu.unifor.missaomarte.servidor.erro.ApiExcecao;
import br.edu.unifor.missaomarte.servidor.repositorio.PartidaRepositorio;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de partidas, histórico e ranking global.
 *
 * Limitação conhecida: o servidor não executa o jogo, então não consegue provar que uma
 * pontuação é legítima. Ele só recusa o que é impossível pela fórmula do jogo
 * (abates×10 + segundos×2 + passageiros×100 + 500 se venceu) e por limites de cada campo.
 */
@Service
public class ServicoPartidas {
    public static final int LIMITE_RANKING = 50;
    private static final int HISTORICO = 20;

    private final PartidaRepositorio partidas;

    public ServicoPartidas(PartidaRepositorio partidas) {
        this.partidas = partidas;
    }

    @Transactional
    public PartidaRegistrada registrar(Jogador jogador, PartidaRequisicao r) {
        if (r.pontuacao() > pontuacaoMaxima(r)) {
            throw ApiExcecao.pontuacaoImplausivel();
        }
        if (r.venceu() && r.passageiros() < 5) {
            throw ApiExcecao.pontuacaoImplausivel();
        }
        return partidas.save(new PartidaRegistrada(jogador, r.pontuacao(), r.dificuldade(), r.tempoSegundos(), r.nivel(),
                r.abates(), r.passageiros(), r.venceu()));
    }

    /** Teto teórico da pontuação para as estatísticas declaradas (mesma fórmula do cliente). */
    static int pontuacaoMaxima(PartidaRequisicao r) {
        return r.abates() * 10 + r.tempoSegundos() * 2 + r.passageiros() * 100 + (r.venceu() ? 500 : 0);
    }

    @Transactional(readOnly = true)
    public List<PartidaRegistrada> ranking(NivelDificuldade dificuldade, int limite) {
        int n = Math.max(1, Math.min(LIMITE_RANKING, limite));
        return partidas.melhores(dificuldade, PageRequest.of(0, n));
    }

    @Transactional(readOnly = true)
    public long posicao(PartidaRegistrada p) {
        return partidas.posicaoDaPontuacao(p.getPontuacao());
    }

    @Transactional(readOnly = true)
    public List<PartidaRegistrada> historico(Jogador jogador) {
        return partidas.findByJogadorOrderByRegistradaEmDesc(jogador, PageRequest.of(0, HISTORICO));
    }

    @Transactional(readOnly = true)
    public long total(Jogador jogador) {
        return partidas.countByJogador(jogador);
    }

    @Transactional(readOnly = true)
    public long vitorias(Jogador jogador) {
        return partidas.countByJogadorAndVenceuTrue(jogador);
    }

    @Transactional(readOnly = true)
    public int melhor(Jogador jogador) {
        return partidas.melhorPontuacao(jogador);
    }
}
