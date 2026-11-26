package br.edu.unifor.missaomarte.servidor.api;

import br.edu.unifor.missaomarte.servidor.api.Dtos.CredenciaisRequisicao;
import br.edu.unifor.missaomarte.servidor.api.Dtos.JogadorResposta;
import br.edu.unifor.missaomarte.servidor.api.Dtos.PerfilResposta;
import br.edu.unifor.missaomarte.servidor.api.Dtos.SessaoResposta;
import br.edu.unifor.missaomarte.servidor.dominio.Jogador;
import br.edu.unifor.missaomarte.servidor.servico.ServicoJogadores;
import br.edu.unifor.missaomarte.servidor.servico.ServicoPartidas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Jogadores", description = "Cadastro, login e perfil")
public class JogadorController {
    private final ServicoJogadores jogadores;
    private final ServicoPartidas partidas;

    public JogadorController(ServicoJogadores jogadores, ServicoPartidas partidas) {
        this.jogadores = jogadores;
        this.partidas = partidas;
    }

    @PostMapping("/jogadores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um jogador (a senha é guardada como hash BCrypt)")
    public JogadorResposta cadastrar(@Valid @RequestBody CredenciaisRequisicao req) {
        return Mapeador.jogador(jogadores.cadastrar(req.nome(), req.senha()));
    }

    @PostMapping("/sessoes")
    @Operation(summary = "Login: devolve um token JWT para usar em Authorization: Bearer")
    public SessaoResposta entrar(@Valid @RequestBody CredenciaisRequisicao req) {
        ServicoJogadores.Sessao s = jogadores.autenticar(req.nome(), req.senha());
        return new SessaoResposta(s.token().valor(), "Bearer", s.token().expiraEm(), Mapeador.jogador(s.jogador()));
    }

    @GetMapping("/jogadores/eu")
    @Operation(summary = "Perfil do jogador logado com estatísticas e histórico", security = @SecurityRequirement(name = "bearer"))
    public PerfilResposta perfil(@AuthenticationPrincipal Jwt token) {
        Jogador j = jogadores.porId(Long.parseLong(token.getSubject()));
        return new PerfilResposta(Mapeador.jogador(j), partidas.total(j), partidas.vitorias(j), partidas.melhor(j),
                partidas.historico(j).stream().map(Mapeador::partida).toList());
    }
}
