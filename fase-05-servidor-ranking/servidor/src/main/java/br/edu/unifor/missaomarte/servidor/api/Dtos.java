package br.edu.unifor.missaomarte.servidor.api;

import br.edu.unifor.missaomarte.servidor.dominio.NivelDificuldade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * Contratos da API (DTOs). As entidades JPA nunca saem do servidor: assim a senha (hash)
 * e os detalhes do banco não vazam, e o formato da API não muda se o banco mudar.
 */
public final class Dtos {
    private Dtos() {
    }

    public record CredenciaisRequisicao(
            @NotBlank @Size(min = 3, max = 16) @Pattern(regexp = "[\\p{L}0-9_ ]+", message = "use letras, números, espaço ou _")
            String nome,
            @NotBlank @Size(min = 6, max = 72) String senha) {
    }

    public record JogadorResposta(Long id, String nome, Instant criadoEm) {
    }

    public record SessaoResposta(String token, String tipo, Instant expiraEm, JogadorResposta jogador) {
    }

    public record PartidaRequisicao(
            @Min(0) @Max(1_000_000) int pontuacao,
            @NotNull NivelDificuldade dificuldade,
            @Min(0) @Max(7_200) int tempoSegundos,
            @Min(1) @Max(500) int nivel,
            @Min(0) @Max(50_000) int abates,
            @Min(0) @Max(5) int passageiros,
            boolean venceu) {
    }

    public record PartidaResposta(Long id, String jogador, int pontuacao, NivelDificuldade dificuldade, int tempoSegundos,
                                  int nivel, int abates, int passageiros, boolean venceu, Instant registradaEm) {
    }

    public record RegistroResposta(PartidaResposta partida, long posicaoGlobal) {
    }

    public record LinhaRanking(int posicao, String jogador, int pontuacao, NivelDificuldade dificuldade,
                               int tempoSegundos, int nivel, boolean venceu, Instant registradaEm) {
    }

    public record PerfilResposta(JogadorResposta jogador, long partidas, long vitorias, int melhorPontuacao,
                                 List<PartidaResposta> ultimasPartidas) {
    }
}
