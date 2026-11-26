package br.edu.unifor.missaomarte.servidor.erro;

import org.springframework.http.HttpStatus;

/** Erro de regra de negócio com status HTTP e código estável para o cliente. */
public class ApiExcecao extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;

    public ApiExcecao(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public static ApiExcecao nomeEmUso() {
        return new ApiExcecao(HttpStatus.CONFLICT, "NOME_EM_USO", "Esse nome de jogador já está em uso.");
    }

    public static ApiExcecao credenciaisInvalidas() {
        return new ApiExcecao(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Nome ou senha incorretos.");
    }

    public static ApiExcecao pontuacaoImplausivel() {
        return new ApiExcecao(HttpStatus.valueOf(422), "PONTUACAO_IMPLAUSIVEL",
                "A pontuação não é compatível com as estatísticas enviadas.");
    }

    public static ApiExcecao jogadorNaoEncontrado() {
        return new ApiExcecao(HttpStatus.UNAUTHORIZED, "JOGADOR_NAO_ENCONTRADO", "Sessão inválida: jogador não existe.");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
