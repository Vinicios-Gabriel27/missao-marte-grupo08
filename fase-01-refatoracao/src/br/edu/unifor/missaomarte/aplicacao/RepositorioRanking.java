package br.edu.unifor.missaomarte.aplicacao;

import java.util.List;

/**
 * Porta de persistência (DIP): a aplicação depende desta interface, não de arquivo,
 * banco ou HTTP. Na Fase 5, o ranking global entra como outra implementação.
 */
public interface RepositorioRanking {
    List<RegistroRanking> listar();

    void salvarTodos(List<RegistroRanking> registros);

    void limpar();
}
