package br.edu.unifor.missaomarte.ranking;

import java.util.List;

/**
 * Porta de persistência do ranking (DIP). Implementações: arquivo local (libGDX) agora,
 * servidor REST na Fase 5 — o serviço não muda.
 */
public interface RepositorioRanking {
    List<RegistroRanking> carregar();

    void salvar(List<RegistroRanking> registros);
}
