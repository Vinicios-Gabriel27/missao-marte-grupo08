package br.edu.unifor.missaomarte.servidor.erro;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Todas as respostas de erro no mesmo formato (RFC 9457 "problem details"):
 * {type, title, status, detail, codigo, campos?}. Nunca devolve stack trace.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(ApiExcecao.class)
    public ProblemDetail regra(ApiExcecao e) {
        return problema(e.getStatus(), e.getCodigo(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Há campos inválidos na requisição.");
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        p.setProperty("campos", campos);
        return p;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail corpoInvalido(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", "Corpo ou parâmetro em formato inválido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflito(DataIntegrityViolationException e) {
        return problema(HttpStatus.CONFLICT, "CONFLITO", "A operação conflita com dados existentes.");
    }

    static ProblemDetail problema(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(status.getReasonPhrase());
        p.setType(URI.create("https://missao-marte/erros/" + codigo.toLowerCase().replace('_', '-')));
        p.setProperty("codigo", codigo);
        return p;
    }
}
