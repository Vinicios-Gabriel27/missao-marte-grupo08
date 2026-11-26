package br.edu.unifor.missaomarte.servidor.api;

import br.edu.unifor.missaomarte.servidor.api.Dtos.LinhaRanking;
import br.edu.unifor.missaomarte.servidor.api.Dtos.PartidaRequisicao;
import br.edu.unifor.missaomarte.servidor.api.Dtos.PartidaResposta;
import br.edu.unifor.missaomarte.servidor.api.Dtos.RegistroResposta;
import br.edu.unifor.missaomarte.servidor.dominio.NivelDificuldade;
import br.edu.unifor.missaomarte.servidor.dominio.PartidaRegistrada;
import br.edu.unifor.missaomarte.servidor.servico.ServicoJogadores;
import br.edu.unifor.missaomarte.servidor.servico.ServicoPartidas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Partidas e ranking")
public class PartidaController {
    private final ServicoJogadores jogadores;
    private final ServicoPartidas partidas;

    public PartidaController(ServicoJogadores jogadores, ServicoPartidas partidas) {
        this.jogadores = jogadores;
        this.partidas = partidas;
    }

    @PostMapping("/partidas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra o resultado de uma partida do jogador logado", security = @SecurityRequirement(name = "bearer"))
    public RegistroResposta registrar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody PartidaRequisicao req) {
        PartidaRegistrada p = partidas.registrar(jogadores.porId(Long.parseLong(token.getSubject())), req);
        return new RegistroResposta(Mapeador.partida(p), partidas.posicao(p));
    }

    @GetMapping("/partidas/minhas")
    @Operation(summary = "Histórico das últimas partidas do jogador logado", security = @SecurityRequirement(name = "bearer"))
    public List<PartidaResposta> minhas(@AuthenticationPrincipal Jwt token) {
        return partidas.historico(jogadores.porId(Long.parseLong(token.getSubject()))).stream().map(Mapeador::partida).toList();
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking global (público). Filtro opcional por dificuldade.")
    public List<LinhaRanking> ranking(@RequestParam(required = false) NivelDificuldade dificuldade,
                                      @RequestParam(defaultValue = "10") int limite) {
        List<LinhaRanking> linhas = new ArrayList<>();
        int posicao = 1;
        for (PartidaRegistrada p : partidas.ranking(dificuldade, limite)) {
            linhas.add(new LinhaRanking(posicao++, p.getJogador().getNome(), p.getPontuacao(), p.getDificuldade(),
                    p.getTempoSegundos(), p.getNivel(), p.isVenceu(), p.getRegistradaEm()));
        }
        return linhas;
    }
}
