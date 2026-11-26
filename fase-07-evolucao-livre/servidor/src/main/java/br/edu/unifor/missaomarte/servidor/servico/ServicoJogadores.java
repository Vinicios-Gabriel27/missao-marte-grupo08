package br.edu.unifor.missaomarte.servidor.servico;

import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import br.edu.unifor.missaomarte.servidor.erro.ApiExcecao;
import br.edu.unifor.missaomarte.servidor.repositorio.JogadorRepositorio;
import br.edu.unifor.missaomarte.servidor.seguranca.EmissorDeToken;
import br.edu.unifor.missaomarte.servidor.seguranca.EmissorDeToken.TokenEmitido;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro e autenticação de jogadores. */
@Service
public class ServicoJogadores {
    /** Hash fixo usado quando o nome não existe, para o login levar o mesmo tempo (evita descobrir nomes). */
    private static final String HASH_FICTICIO = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWX4Z1Bo0GZ2Iqz9C9zQd3Wl3S1e";

    private final JogadorRepositorio jogadores;
    private final PasswordEncoder senhas;
    private final EmissorDeToken emissor;

    public ServicoJogadores(JogadorRepositorio jogadores, PasswordEncoder senhas, EmissorDeToken emissor) {
        this.jogadores = jogadores;
        this.senhas = senhas;
        this.emissor = emissor;
    }

    @Transactional
    public Jogador cadastrar(String nome, String senha) {
        String limpo = nome.trim();
        if (jogadores.existsByNomeIgnoreCase(limpo)) {
            throw ApiExcecao.nomeEmUso();
        }
        return jogadores.save(new Jogador(limpo, senhas.encode(senha)));
    }

    public record Sessao(Jogador jogador, TokenEmitido token) {
    }

    @Transactional(readOnly = true)
    public Sessao autenticar(String nome, String senha) {
        Jogador jogador = jogadores.findByNomeIgnoreCase(nome.trim()).orElse(null);
        boolean confere = senhas.matches(senha, jogador == null ? HASH_FICTICIO : jogador.getSenhaHash());
        if (jogador == null || !confere) {
            throw ApiExcecao.credenciaisInvalidas();
        }
        return new Sessao(jogador, emissor.emitir(jogador));
    }

    @Transactional(readOnly = true)
    public Jogador porId(long id) {
        return jogadores.findById(id).orElseThrow(ApiExcecao::jogadorNaoEncontrado);
    }
}
