package br.edu.unifor.missaomarte.online;

import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import java.util.List;

/**
 * Porta para o servidor de ranking (DIP). O jogo depende desta interface; a implementação
 * HTTP fica na camada libGDX e pode ser trocada por um dublê nos testes.
 * Todas as operações são assíncronas: nunca bloqueiam a thread que desenha o jogo.
 */
public interface ServicoOnline {
    void cadastrar(String nome, String senha, Resposta<Void> resposta);

    void entrar(String nome, String senha, Resposta<Sessao> resposta);

    /** Envia a partida; responde com a posição no ranking global. */
    void enviarPartida(Sessao sessao, RegistroRanking partida, Resposta<Long> resposta);

    void rankingGlobal(Resposta<List<RegistroRanking>> resposta);
}
