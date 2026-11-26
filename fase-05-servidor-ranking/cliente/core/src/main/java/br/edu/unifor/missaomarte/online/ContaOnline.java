package br.edu.unifor.missaomarte.online;

import br.edu.unifor.missaomarte.ranking.RegistroRanking;
import java.util.Optional;

/**
 * Estado da conta: convidado (padrão, joga e salva só localmente) ou logado (também envia ao
 * ranking global). Concentra as regras para a interface não precisar conhecer o servidor.
 */
public class ContaOnline {
    private final ServicoOnline servico;
    private Sessao sessao;

    public ContaOnline(ServicoOnline servico) {
        this.servico = servico;
    }

    public boolean logado() {
        return sessao != null;
    }

    public Optional<String> nome() {
        return Optional.ofNullable(sessao).map(Sessao::nome);
    }

    public void entrar(String nome, String senha, Resposta<Sessao> resposta) {
        servico.entrar(nome, senha, new Resposta<>() {
            @Override
            public void sucesso(Sessao s) {
                sessao = s;
                resposta.sucesso(s);
            }

            @Override
            public void falha(String mensagem) {
                resposta.falha(mensagem);
            }
        });
    }

    /** Cadastra e já entra com a conta nova. */
    public void cadastrarEEntrar(String nome, String senha, Resposta<Sessao> resposta) {
        servico.cadastrar(nome, senha, new Resposta<>() {
            @Override
            public void sucesso(Void v) {
                entrar(nome, senha, resposta);
            }

            @Override
            public void falha(String mensagem) {
                resposta.falha(mensagem);
            }
        });
    }

    public void sair() {
        sessao = null;
    }

    /**
     * Envia a partida se estiver logado. Convidado: não faz nada e devolve false.
     * Se o token expirou (401), volta para convidado.
     */
    public boolean enviar(RegistroRanking partida, Resposta<Long> resposta) {
        if (sessao == null) {
            return false;
        }
        servico.enviarPartida(sessao, partida, new Resposta<>() {
            @Override
            public void sucesso(Long posicao) {
                resposta.sucesso(posicao);
            }

            @Override
            public void falha(String mensagem) {
                if (mensagem != null && mensagem.startsWith("Sessão expirada")) {
                    sessao = null;
                }
                resposta.falha(mensagem);
            }
        });
        return true;
    }

    public ServicoOnline servico() {
        return servico;
    }
}
